@file:JvmName("MessagingJVM")

package cc.mewcraft.wakame.messaging

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.messaging.packet.NationSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.NationSpawnResponsePacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnResponsePacket
import cc.mewcraft.wakame.util.ExceptionLoggingScheduledThreadPoolExecutor
import cc.mewcraft.wakame.util.Exceptions
import cc.mewcraft.wakame.util.concurrent.ConcurrentUtil
import ninja.egg82.messenger.*
import ninja.egg82.messenger.handler.AbstractServerMessagingHandler
import ninja.egg82.messenger.handler.MessagingHandler
import ninja.egg82.messenger.handler.MessagingHandlerImpl
import ninja.egg82.messenger.packets.AbstractPacket
import ninja.egg82.messenger.packets.MultiPacket
import ninja.egg82.messenger.packets.Packet
import ninja.egg82.messenger.packets.server.*
import ninja.egg82.messenger.services.PacketService
import java.io.File
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier

class MessagingManager(
    private val config: MessagingConfig,
) {

    companion object {

        private const val PROTOCOL_VERSION: Byte = 1

        private lateinit var instance: MessagingManager

        /**
         * 初始化全局 [MessagingManager] 实例.
         */
        fun init(config: MessagingConfig) {
            instance = MessagingManager(config)
        }

        /**
         * 初始化消息服务.
         */
        fun start() = instance.start()

        /**
         * 关闭消息服务.
         */
        fun shutdown() = instance.shutdown()

        /**
         * 获取 serverId.
         */
        val serverId: UUID get() = instance.serverId

        /**
         * 获取 serverKey.
         */
        val serverKey: String get() = instance.serverKey

        /**
         * 获取 serverGroup.
         */
        val serverGroup: String get() = instance.serverGroup

        /**
         * 队列数据包.
         */
        fun queuePacketAndFlush(makePacket: Supplier<out AbstractPacket>) {
            instance.queuePacketAndFlush(makePacket)
        }

        /**
         * 队列数据包.
         */
        fun queuePacket(makePacket: Supplier<out AbstractPacket>) {
            instance.queuePacket(makePacket)
        }
    }

    /**
     * @see ServerInfoProvider.serverId
     */
    val serverId: UUID
        get() = ServerInfoProvider.serverId

    /**
     * @see ServerInfoProvider.serverKey
     */
    val serverKey: String
        get() = ServerInfoProvider.serverKey

    /**
     * @see ServerInfoProvider.serverGroup
     */
    val serverGroup: String
        get() = ServerInfoProvider.serverGroup

    private lateinit var scheduledExecutor: ScheduledExecutorService
    private lateinit var messagingService: MessagingService

    @Volatile
    private lateinit var packetService: PacketService

    /**
     * 启动消息服务.
     */
    fun start() {
        if (config.enabled) {
            LOGGER.info("Initializing messaging service...")
        } else {
            LOGGER.info("Messaging service disabled in config.")
            return
        }

        /* 注册封包 */

        // 基础封包类型 (没这些框架跑不起来)
        registerPacket(::MultiPacket)
        registerPacket(::KeepAlivePacket)
        registerPacket(::InitializationPacket)
        registerPacket(::PacketVersionPacket)
        registerPacket(::PacketVersionRequestPacket)
        registerPacket(::ShutdownPacket)

        // 自定义封包类型, 每个都得注册, 按需更新这里
        registerPacket(::TownSpawnRequestPacket)
        registerPacket(::TownSpawnResponsePacket)
        registerPacket(::NationSpawnRequestPacket)
        registerPacket(::NationSpawnResponsePacket)

        this.packetService = PacketService(4, false, PROTOCOL_VERSION)
        this.scheduledExecutor = ExceptionLoggingScheduledThreadPoolExecutor(4, ConcurrentUtil.koishThreadFactory(LOGGER, "MessagingManager"), LOGGER)

        val handler = MessagingHandlerImpl(this.packetService)

        // 添加消息处理器
        handler.addHandler(KoishServerMessagingHandler(this.serverId, this.packetService, handler))
        handler.addHandler(MessagingHandler(this.serverId, this.packetService))

        this.messagingService = try {
            initMessagingService(this.packetService, handler, File("/"))
        } catch (e: Exception) {
            throw Exceptions.rethrow<Exception>(e)
        }

        this.packetService.addMessenger(this.messagingService)

        this.packetService.queuePacket(InitializationPacket(serverId, PROTOCOL_VERSION))
        this.packetService.flushQueue()

        // 开始固定频率广播心跳包
        this.scheduledExecutor.scheduleAtFixedRate({
            this.packetService.queuePacket(KeepAlivePacket(serverId))
            this.packetService.flushQueue()
        }, 5, 5, TimeUnit.SECONDS)

        // 开始固定频率刷新数据包队列
        this.scheduledExecutor.scheduleAtFixedRate({
            try {
                this.packetService.flushQueue()
            } catch (_: IndexOutOfBoundsException) {
            }
        }, 0, 250, TimeUnit.MILLISECONDS)
    }

    /**
     * 关闭消息服务并释放资源.
     */
    fun shutdown() {
        if (this::scheduledExecutor.isInitialized) {
            ConcurrentUtil.shutdownExecutor(this.scheduledExecutor, TimeUnit.MILLISECONDS, 500)
        }
        if (this::packetService.isInitialized) {
            this.packetService.flushQueue()
            this.packetService.shutdown()
        }
        if (this::messagingService.isInitialized) {
            this.messagingService.close()
        }
    }

    fun queuePacketAndFlush(makePacket: Supplier<out AbstractPacket>) {
        this.withPacketService { service: PacketService ->
            service.queuePacket(makePacket.get())
            service.flushQueue()
        }
    }

    fun queuePacket(makePacket: Supplier<out AbstractPacket>) {
        this.withPacketService { service: PacketService -> service.queuePacket(makePacket.get()) }
    }

    private fun initMessagingService(
        packetService: PacketService,
        handlerImpl: MessagingHandlerImpl,
        packetDir: File,
    ): MessagingService {
        val name = "engine1"
        val channelName = "koish-core"

        val messagingService = when (config.brokerType) {
            MessagingConfig.BrokerType.NONE -> {
                throw IllegalStateException("MessagingManager initialized with no messaging broker selected!")
            }

            MessagingConfig.BrokerType.NATS -> {
                LOGGER.info("Initializing NATS messaging service...")

                val host = config.NATS().host
                val port = config.NATS().port
                val credentialsFile = config.NATS().credentialsFile
                val builder = NATSMessagingService.builder(packetService, name, channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                    .url(host, port)
                    .life(5000)
                if (credentialsFile != null && credentialsFile.isNotBlank()) {
                    builder.credentials(credentialsFile)
                }
                builder.build()
            }

            MessagingConfig.BrokerType.RABBITMQ -> {
                LOGGER.info("Initializing RABBITMQ messaging service...")

                val host = config.RabbitMQ().host
                val port = config.RabbitMQ().port
                val vhost = config.RabbitMQ().vhost
                val username = config.RabbitMQ().username
                val password = config.RabbitMQ().password
                val builder = RabbitMQMessagingService
                    .builder(packetService, name, channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                    .url(host, port, vhost)
                if (password != null && password.isNotBlank()) {
                    builder.credentials(username, password)
                }
                builder.build()
            }

            MessagingConfig.BrokerType.REDIS -> {
                LOGGER.info("Initializing REDIS messaging service...")

                val host = config.Redis().host
                val port = config.Redis().port
                val password = config.Redis().password
                val builder = RedisMessagingService
                    .builder(packetService, name, channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                    .url(host, port)
                if (password != null && password.isNotBlank()) {
                    builder.credentials(password)
                }
                builder.build()
            }
        }

        return messagingService
    }

    private inline fun <reified T : Packet> registerPacket(supplier: PacketSupplier<T>) {
        PacketManager.register(T::class.java, supplier)
    }

    private fun withPacketService(consumer: Consumer<PacketService>) {
        if (this::packetService.isInitialized) {
            consumer.accept(this.packetService)
        }
    }

    private class KoishServerMessagingHandler(
        serverId: UUID,
        packetService: PacketService,
        messagingHandler: MessagingHandler,
    ) : AbstractServerMessagingHandler(serverId, packetService, messagingHandler) {

        override fun handleInitialization(packet: InitializationPacket) {
            super.handleInitialization(packet)
            LOGGER.info("Received initialization packet from server ${packet.server}")
        }
    }
}
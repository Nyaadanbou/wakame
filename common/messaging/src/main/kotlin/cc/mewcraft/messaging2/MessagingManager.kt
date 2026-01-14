package cc.mewcraft.messaging2

import cc.mewcraft.messaging2.util.ConcurrentUtil
import cc.mewcraft.messaging2.util.ExceptionLoggingScheduledThreadPoolExecutor
import cc.mewcraft.messaging2.util.Exceptions
import ninja.egg82.messenger.*
import ninja.egg82.messenger.handler.AbstractMessagingHandler
import ninja.egg82.messenger.handler.AbstractServerMessagingHandler
import ninja.egg82.messenger.handler.MessagingHandler
import ninja.egg82.messenger.handler.MessagingHandlerImpl
import ninja.egg82.messenger.packets.AbstractPacket
import ninja.egg82.messenger.packets.MultiPacket
import ninja.egg82.messenger.packets.Packet
import ninja.egg82.messenger.packets.server.*
import ninja.egg82.messenger.services.PacketService
import org.slf4j.Logger
import java.io.File
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier


interface StaticAccessApi {

    fun init(config: MessagingConfig)
    fun start()
    fun shutdown()
    fun queuePacketAndFlush(makePacket: Supplier<out AbstractPacket>)
    fun queuePacket(makePacket: Supplier<out AbstractPacket>)

    companion object {
        fun of(factory: (MessagingConfig) -> MessagingManager): StaticAccessApi {
            return StaticAccessApiImpl(factory)
        }
    }
}

private class StaticAccessApiImpl(
    private val factory: (MessagingConfig) -> MessagingManager,
) : StaticAccessApi {

    private lateinit var instance: MessagingManager

    override fun init(config: MessagingConfig) {
        instance = factory(config)
    }

    override fun start() {
        instance.start()
    }

    override fun shutdown() {
        instance.shutdown()
    }

    override fun queuePacketAndFlush(makePacket: Supplier<out AbstractPacket>) {
        instance.queuePacketAndFlush(makePacket)
    }

    override fun queuePacket(makePacket: Supplier<out AbstractPacket>) {
        instance.queuePacket(makePacket)
    }
}

typealias AbstractMessagingManager = MessagingManager

abstract class MessagingManager(
    private val config: MessagingConfig,
) {

    val serverId: UUID
        get() = ServerInfoProvider.serverId
    val serverKey: String
        get() = ServerInfoProvider.serverKey
    val serverGroup: String
        get() = ServerInfoProvider.serverGroup

    protected abstract val logger: Logger
    protected abstract val channelName: String
    protected abstract val protocolVersion: Byte

    protected abstract fun registerPackets()
    protected abstract fun registerMessagingHandlers(addHandler: (AbstractMessagingHandler) -> Unit)

    protected lateinit var scheduledExecutor: ScheduledExecutorService
    protected lateinit var messagingService: MessagingService
    @Volatile
    protected lateinit var packetService: PacketService

    /**
     * 启动消息服务.
     */
    fun start() {
        if (config.enabled) {
            logger.info("Initializing messaging service...")
        } else {
            logger.info("Messaging service disabled in config.")
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
        registerPackets()

        this.packetService = PacketService(4, false, this.protocolVersion)
        this.scheduledExecutor = ExceptionLoggingScheduledThreadPoolExecutor(4, ConcurrentUtil.koishThreadFactory(logger, "MessagingManager"), logger)

        val handler = MessagingHandlerImpl(this.packetService)

        /* 添加消息处理器 */

        // 核心消息处理器
        handler.addHandler(ServerMessagingHandler(this.serverId, this.packetService, handler))
        // 自定义消息处理器
        registerMessagingHandlers(handler::addHandler)

        this.messagingService = try {
            initMessagingService(this.packetService, handler, File("/"))
        } catch (e: Exception) {
            throw Exceptions.rethrow<Exception>(e)
        }

        this.packetService.addMessenger(this.messagingService)

        this.packetService.queuePacket(InitializationPacket(this.serverId, this.protocolVersion))
        this.packetService.flushQueue()

        // 开始固定频率广播心跳包
        this.scheduledExecutor.scheduleAtFixedRate({
            this.packetService.queuePacket(KeepAlivePacket(this.serverId))
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

    /**
     * 子类使用这个来注册新的封包类型.
     */
    protected inline fun <reified T : Packet> registerPacket(supplier: PacketSupplier<T>) {
        PacketManager.register(T::class.java, supplier)
    }

    private fun initMessagingService(
        packetService: PacketService,
        handlerImpl: MessagingHandlerImpl,
        packetDir: File,
    ): MessagingService {
        val name = "engine1"

        val messagingService = when (config.brokerType) {
            MessagingConfig.BrokerType.NONE -> {
                throw IllegalStateException("MessagingManager initialized with no messaging broker selected!")
            }

            MessagingConfig.BrokerType.NATS -> {
                logger.info("Initializing NATS messaging service...")

                val host = config.NATS().host
                val port = config.NATS().port
                val credentialsFile = config.NATS().credentialsFile
                val builder = NATSMessagingService.builder(packetService, name, this.channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                    .url(host, port)
                    .life(5000)
                if (credentialsFile != null && credentialsFile.isNotBlank()) {
                    builder.credentials(credentialsFile)
                }
                builder.build()
            }

            MessagingConfig.BrokerType.RABBITMQ -> {
                logger.info("Initializing RABBITMQ messaging service...")

                val host = config.RabbitMQ().host
                val port = config.RabbitMQ().port
                val vhost = config.RabbitMQ().vhost
                val username = config.RabbitMQ().username
                val password = config.RabbitMQ().password
                val builder = RabbitMQMessagingService
                    .builder(packetService, name, this.channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                    .url(host, port, vhost)
                if (password != null && password.isNotBlank()) {
                    builder.credentials(username, password)
                }
                builder.build()
            }

            MessagingConfig.BrokerType.REDIS -> {
                logger.info("Initializing REDIS messaging service...")

                val host = config.Redis().host
                val port = config.Redis().port
                val password = config.Redis().password
                val builder = RedisMessagingService
                    .builder(packetService, name, this.channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                    .url(host, port)
                if (password != null && password.isNotBlank()) {
                    builder.credentials(password)
                }
                builder.build()
            }
        }

        return messagingService
    }

    private fun withPacketService(consumer: Consumer<PacketService>) {
        if (this::packetService.isInitialized) {
            consumer.accept(this.packetService)
        }
    }

    private class ServerMessagingHandler(
        serverId: UUID,
        packetService: PacketService,
        messagingHandler: MessagingHandler,
    ) : AbstractServerMessagingHandler(serverId, packetService, messagingHandler) {

        override fun handleInitialization(packet: InitializationPacket) {
            super.handleInitialization(packet)
            logger.info("Received initialization packet from server ${packet.server}")
        }
    }
}
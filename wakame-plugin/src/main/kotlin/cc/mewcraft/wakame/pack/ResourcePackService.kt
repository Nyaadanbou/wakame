package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.sun.net.httpserver.HttpExchange
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.spongepowered.configurate.reference.WatchServiceListener
import team.unnamed.creative.BuiltResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.server.ResourcePackServer
import team.unnamed.creative.server.request.ResourcePackDownloadRequest
import xyz.xenondevs.commons.provider.Provider
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * 负责分发资源包给进入服务器的玩家.
 *
 * 目前有以下实现:
 *
 * ## [NoneService]
 * 什么都不做.
 *
 * ## [SelfHostService]
 * 启动一个内置的 HTTP 服务器, 并自动生成相应的资源包下载链接.
 * 当玩家进入服务器时, 会把资源包的下载链接发送给玩家.
 * 下载资源包的请求将由内置的 HTTP 服务器处理.
 *
 * ## [OnlyURLService]
 * 仅把资源包的下载信息分发给玩家, 无内置 HTTP 服务器.
 * 用户需要自行提供 *完整且有效的* 资源包下载链接.
 * 如果下载链接无效, 将不会发送资源包给玩家.
 */
sealed interface ResourcePackService {
    /**
     * 启动服务.
     */
    fun start()

    /**
     * 停止服务.
     */
    fun stop()

    /**
     * 将资源包发送给玩家.
     */
    fun sendPack(player: Player)
}

/**
 * 负责提供 [ResourcePackService] 实例给外部使用.
 */
object ResourcePackServiceProvider {
    private var INSTANCE: ResourcePackService? = null

    /**
     * 获取当前的 [ResourcePackService] 实例.
     *
     * 注意:
     * - 如果当前没有已加载的实例, 会尝试加载一个新的实例.
     * - 该函数不负责启动新创建的服务.
     * - 非线程安全.
     */
    fun get(): ResourcePackService {
        return INSTANCE ?: set()
    }

    /**
     * 重新加载并设置新的 [ResourcePackService] 实例.
     *
     * 注意:
     * - 该函数会先尝试停止当前的服务, 然后再加载新的服务.
     * - 该函数不负责启动新创建的服务.
     * - 非线程安全.
     */
    fun set(): ResourcePackService {
        // 停止旧实例
        INSTANCE?.stop()

        val config = RESOURCE_PACK_CONFIG.node("service")
        val required = config.entry<Boolean>("required")
        val prompt = config.entry<Component>("prompt")
        val instance = when (
            val type = config.entry<String>("type").get()
        ) {
            "none" -> {
                NoneService
            }

            "self_host" -> {
                val host = config.entry<String>("host")
                val port = config.entry<Int>("port")
                SelfHostService(required, prompt, host, port)
            }

            "only_url" -> {
                val downloadURL = config.entry<String>("download_url")
                OnlyURLService(required, prompt, downloadURL)
            }

            else -> {
                throw IllegalArgumentException("Unknown service type: '$type'")
            }
        }

        return instance.also {
            INSTANCE = it // 设置新实例
        }
    }
}


/* Internals */


/**
 * 代表一个空的服务, 什么都不做.
 */
private data object NoneService : ResourcePackService {
    override fun start() = Unit // do nothing
    override fun stop() = Unit // do nothing
    override fun sendPack(player: Player) = Unit // do nothing
}

/**
 * 使用内置的 HTTP 服务器分发资源包给玩家.
 */
private class SelfHostService(
    // shared
    required: Provider<Boolean>,
    prompt: Provider<Component>,
    // adhoc
    host: Provider<String>,
    port: Provider<Int>,
) : ResourcePackService {

    // HTTP 服务器监听的主机名
    private val host: String by host

    // HTTP 服务器监听的端口
    private val port: Int by port

    // 是否要求玩家下载资源包
    private val required: Boolean by required

    // 玩家下载资源包的提示信息
    private val prompt: Component by prompt

    // HTTP 服务器的 Executor
    private val executor: ExecutorService = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("Koish-Self-Host-Service-%d").build())

    // HTTP 服务器实例
    private val httpServer: ResourcePackServer = ResourcePackServer.server()
        // 端口设置为用户指定的, 但 IP 永远绑定到 0.0.0.0 (IPv4)
        // 经过测试, 使用 ::0 (IPv6) 会导致客户端无法下载资源包.
        // 必须添加启动参数 -Djava.net.preferIPv4Stack=true
        .address(this.port)
        .handler(this::handleRequest)
        .executor(this.executor)
        .build()

    // 资源包请求, 将发送给玩家
    private val resourcePackRequest: ResourcePackRequest
        // 使用 getter 使得每次返回一个新的 ResourcePackRequest
        get() {
            // 构建下载链接
            val hash = builtResourcePack?.hash() ?: return ResourcePackRequest.resourcePackRequest().build()
            val downloadURL = if (port != 80) {
                "http://$host:$port"
            } else {
                "http://$host"
            }

            // 构建 ResourcePackInfo
            val id = UUID.nameUUIDFromBytes(downloadURL.encodeToByteArray())
            val uri = URI.create(downloadURL)
            val packInfo = ResourcePackInfo.resourcePackInfo().id(id).uri(uri).hash(hash)

            // 构建 ResourcePackRequest
            val request = ResourcePackRequest.resourcePackRequest()
                .packs(packInfo)
                .required(required)
                .prompt(prompt)
                .replace(true)
                .build()

            return request
        }

    // 当前生成好的资源包
    private var builtResourcePack: BuiltResourcePack? = buildResourcePack()

    private val watchServiceListener: WatchServiceListener = WatchServiceListener.create()

    private fun handleRequest(request: ResourcePackDownloadRequest?, exchange: HttpExchange) {
        // request == null 的情况一般就是用浏览器直接下载资源包.
        // 目前没必要提供浏览器下载资源包的功能, 所以直接返回 400.
        //
        // 如果是游戏客户端发起的请求, 只要客户端版本对的上服务器的,
        // request 就不会是 null.
        if (request == null) {
            val data = "Please use a Minecraft client\n".toByteArray(Charsets.UTF_8)
            exchange.responseHeaders["Content-Type"] = "text/plain"
            exchange.sendResponseHeaders(400, data.size.toLong())
            exchange.responseBody.use { responseStream ->
                responseStream.write(data)
            }
            return
        }

        // 如果资源包还未生成, 返回 404
        val pack = builtResourcePack
        if (pack == null) {
            val response = "Resourcepack is not generated yet.\n".toByteArray()
            exchange.responseHeaders["Content-Type"] = "text/plain"
            exchange.sendResponseHeaders(404, response.size.toLong())
            exchange.responseBody.use { responseStream -> responseStream.write(response) }
            return
        }

        // 如果资源包已经生成, 返回资源包
        val packData = pack.data().toByteArray()
        exchange.responseHeaders["Content-Type"] = "application/zip"
        exchange.sendResponseHeaders(200, packData.size.toLong())
        exchange.responseBody.use { responseStream -> responseStream.write(packData) }
    }

    private fun computeHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream()
            .buffered()
            .use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
        return bytesToString(digest.digest())
    }

    private fun bytesToString(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun buildResourcePack(): BuiltResourcePack? {
        val file = Injector.get<File>(InjectionQualifier.DATA_FOLDER).resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
        if (!file.exists() || !file.isFile) {
            LOGGER.warn("Resource pack file not found at: '${file.path}'. No resource pack will be sent to players.")
            return null
        }
        val hash = computeHash(file)
        val data = Writable.file(file)

        return BuiltResourcePack.of(data, hash)
    }

    override fun start() {
        LOGGER.info("Starting resource pack http server. Port: $port")

        watchServiceListener.listenToFile(
            Injector.get<Path>(InjectionQualifier.DATA_FOLDER).resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
        ) { event ->
            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY || event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                LOGGER.info("Resource pack file changed. Reloading resource pack.")
                builtResourcePack = buildResourcePack()
                LOGGER.info("Resource pack reloaded.")
            }
        }

        httpServer.start()
    }

    override fun stop() {
        LOGGER.info("Stopping resource pack http server at port $port")
        watchServiceListener.close()
        httpServer.stop(10)
    }

    override fun sendPack(player: Player) {
        player.sendResourcePacks(resourcePackRequest)
    }
}

/**
 * 仅把资源包的下载链接分发给玩家, 无内置 HTTP 服务器.
 */
private class OnlyURLService(
    // shared
    required: Provider<Boolean>,
    prompt: Provider<Component>,
    // adhoc
    downloadURL: Provider<String>,
) : ResourcePackService {

    // 要求玩家必须安装资源包?
    private val required: Boolean by required

    // 玩家安装资源包后, 弹出的提示信息
    private val prompt: Component by prompt

    // 资源包的下载链接
    private val downloadURL: String by downloadURL

    private val resourcePackRequest: ResourcePackRequest
        get() {
            // 构建 ResourcePackInfo
            val id = UUID.nameUUIDFromBytes(downloadURL.encodeToByteArray())
            val uri = URI.create(downloadURL)
            val packInfo = try {
                ResourcePackInfo.resourcePackInfo().id(id).uri(uri).computeHashAndBuild().get(10, TimeUnit.SECONDS)
            } catch (e: Exception) {
                LOGGER.error("Failed to compute hash for resource pack: '$downloadURL'. No resource pack will be sent to players.", e)
                // 返回一个空的 ResourcePackRequest
                return ResourcePackRequest.resourcePackRequest().build()
            }

            // 构建 ResourcePackRequest
            val request = ResourcePackRequest.resourcePackRequest()
                .packs(packInfo)
                .required(required)
                .prompt(prompt)
                .replace(true)
                .build()
            return request
        }

    override fun start() = Unit // do nothing
    override fun stop() = Unit // do nothing
    override fun sendPack(player: Player) {
        player.sendResourcePacks(resourcePackRequest)
    }
}

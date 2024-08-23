package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import com.sun.net.httpserver.HttpExchange
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import team.unnamed.creative.BuiltResourcePack
import team.unnamed.creative.server.ResourcePackServer
import team.unnamed.creative.server.request.ResourcePackDownloadRequest
import java.lang.reflect.Type
import java.net.URI
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * 负责直接地向玩家发送资源包.
 *
 * ## 无分发 ([NoneService])
 * 什么都不做.
 *
 * ## 直接分发 ([BuiltInService])
 * 通过直接构建一个内置的 HTTP 服务器, 将资源包分发给玩家.
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
    fun sendToPlayer(player: Player)
}

/**
 * 代表一个空的服务, 什么都不做.
 */
data object NoneService : ResourcePackService {
    override fun start() = Unit // do nothing
    override fun stop() = Unit // do nothing
    override fun sendToPlayer(player: Player) = Unit // do nothing
}

/**
 * 使用内置的 HTTP 服务器分发资源包给玩家.
 */
private data class BuiltInService(
    private val host: String,
    private val port: Int,
    private val appendPort: Boolean,
) : ResourcePackService, KoinComponent {
    companion object {
        private val RESOURCEPACK_UUID = UUID.fromString("0191800e-4242-7d9b-93c2-a03bcb35b59d")
    }

    private val logger: Logger by inject()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private val resourcePack: BuiltResourcePack?
        get() = ServiceSupport.resourcePackManager.pack

    private var server: ResourcePackServer? = null

    private fun handleRequest(request: ResourcePackDownloadRequest?, exchange: HttpExchange) {
        if (request == null) {
            exchange.sendResponseHeaders(400, 0)
            return
        }
        // 如果资源包已经生成, 则直接发送, 否则返回 404
        val resourcePack = resourcePack
        if (resourcePack == null) {
            val response = "Resourcepack is not generated yet.\n".toByteArray()
            exchange.responseHeaders["Content-Type"] = "text/plain"
            exchange.sendResponseHeaders(404, response.size.toLong())
            exchange.responseBody.use { responseStream -> responseStream.write(response) }
            return
        }
        val data: ByteArray = resourcePack.data().toByteArray()
        exchange.responseHeaders["Content-Type"] = "application/zip"
        exchange.sendResponseHeaders(200, data.size.toLong())
        exchange.responseBody.use { responseStream -> responseStream.write(data) }
    }

    private fun buildServer(): ResourcePackServer {
        return ResourcePackServer.server()
            .address(port)
            .handler(::handleRequest)
            .executor(executor)
            .build()
    }

    override fun start() {
        logger.info("Starting resource pack server. Port: $port")
        server = buildServer()
        server?.start()
    }

    override fun stop() {
        logger.info("Stopping resource pack server")
        server?.stop(0)
    }

    override fun sendToPlayer(player: Player) {
        val resourcePack = resourcePack ?: return
        val downloadUrl: String = if (appendPort) {
            "http://$host:$port/${resourcePack.hash()}.zip"
        } else {
            "http://$host/${resourcePack.hash()}.zip"
        }
        val request = ResourcePackRequest.resourcePackRequest()
            .packs(ResourcePackInfo.resourcePackInfo(RESOURCEPACK_UUID, URI.create(downloadUrl), resourcePack.hash()))
            .required(true)
            .prompt(null)
            .replace(true)
        player.sendResourcePacks(request)
    }
}

private object ServiceSupport : KoinComponent {
    val resourcePackManager: ResourcePackManager by inject()
}

internal object ResourcePackServiceSerializer : TypeSerializer<ResourcePackService> {
    override fun deserialize(type: Type, node: ConfigurationNode): ResourcePackService {
        return when (node.node("type").krequire<String>().lowercase()) {
            "self_host" -> {
                val host = node.node("host").krequire<String>()
                val port = node.node("port").krequire<Int>()
                val appendPort = node.node("append_port").krequire<Boolean>()
                return BuiltInService(host, port, appendPort)
            }

            else -> NoneService
        }
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ResourcePackService? {
        return NoneService
    }
}
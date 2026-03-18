package cc.mewcraft.wakame.monetization

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.entryOrElse
import org.spongepowered.configurate.objectmapping.ConfigSerializable

private val MONETIZATION_CONFIG = ConfigAccess["monetization"]

object MonetizationConfig {
    val version by MONETIZATION_CONFIG.entryOrElse<Int>(1, "version")
    val enabled by MONETIZATION_CONFIG.entryOrElse<Boolean>(false, "enabled")
    val zPayApi by MONETIZATION_CONFIG.entryOrElse<ZPayApi>(ZPayApi(), "z_pay_api")
    val callbackServer by MONETIZATION_CONFIG.entryOrElse<CallbackServer>(CallbackServer(), "callback_server")
    val order by MONETIZATION_CONFIG.entryOrElse<OrderConfig>(OrderConfig(), "order")
}

@ConfigSerializable
data class ZPayApi(
    val url: String = "https://zpayz.cn/",
    val pid: String = "0000000000000000",
    val pkey: String = "AAAAAAAAAAAAAAAA",
)

@ConfigSerializable
data class CallbackServer(
    /** 内嵌 HTTP 服务器监听地址. */
    val host: String = "0.0.0.0",
    /** 内嵌 HTTP 服务器监听端口. */
    val port: Int = 18080,
    /** Z-PAY 异步回调的完整公网 URL (必须外网可达). */
    val notifyUrl: String = "http://your-server-ip:18080/zpay/notify",
)

@ConfigSerializable
data class OrderConfig(
    /** 订单超时时间 (秒). 超过此时间未支付的订单将被标记为过期. */
    val timeoutSeconds: Long = 300,
    /** 同一玩家最多同时存在的待支付订单数. */
    val maxPendingPerPlayer: Int = 1,
)


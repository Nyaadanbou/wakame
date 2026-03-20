package cc.mewcraft.wakame.monetization

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.entryOrElse
import org.spongepowered.configurate.objectmapping.ConfigSerializable

private val MONETIZATION_CONFIG = ConfigAccess["monetization"]

object MonetizationConfig {
    val version by MONETIZATION_CONFIG.entryOrElse<Int>(1, "version")
    val enabled by MONETIZATION_CONFIG.entryOrElse<Boolean>(false, "enabled")
    val enabledServer by MONETIZATION_CONFIG.entryOrElse<String>("test", "enabled_server")
    val zPayApi by MONETIZATION_CONFIG.entryOrElse<ZPayApi>(ZPayApi(), "z_pay_api")
    val callbackServer by MONETIZATION_CONFIG.entryOrElse<CallbackServer>(CallbackServer(), "callback_server")
    val order by MONETIZATION_CONFIG.entryOrElse<OrderConfig>(OrderConfig(), "order")
    val storage by MONETIZATION_CONFIG.entryOrElse<StorageType>(StorageType.DATABASE, "storage")
    val qrcodeDisplay by MONETIZATION_CONFIG.entryOrElse<QrcodeDisplay>(QrcodeDisplay(), "qrcode_display")
    val luckpermsIntegration by MONETIZATION_CONFIG.entryOrElse<LuckPermsIntegration>(LuckPermsIntegration(), "luckperms_integration")
}

/**
 * 订单存储方式.
 */
enum class StorageType {
    /** 内存存储, 服务器重启后数据丢失 (适合调试). */
    IN_MEMORY,

    /** 数据库持久化, 使用全局数据库连接 (配置见 database.yml, 支持 SQLite / MariaDB). */
    DATABASE,
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

@ConfigSerializable
data class QrcodeDisplay(
    /** 二维码在地图上的显示尺寸 (像素, 1~128). 小于 128 时居中显示, 周围留白. */
    val mapSize: Int = 128,
    /** 二维码展示时长 (秒). 超时后自动结束展示. */
    val displayDuration: Long = 120,
    /** 聊天提示的周期间隔 (秒). */
    val reminderInterval: Long = 3,
)

@ConfigSerializable
data class LuckPermsIntegration(
    /** 是否启用 LuckPerms 集成. */
    val enabled: Boolean = true,
    /** 累积充值金额档位 (单位: 元). */
    val paidAboveThresholds: List<Int> = listOf(60, 240, 480, 720),
)
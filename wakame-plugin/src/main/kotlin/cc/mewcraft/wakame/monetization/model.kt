package cc.mewcraft.wakame.monetization

import java.time.Instant
import java.util.*

/**
 * 支付方式.
 */
enum class PaymentType(val zpayValue: String) {
    ALIPAY("alipay"),
    WXPAY("wxpay"),
    ;

    companion object {
        fun fromZPayValue(value: String): PaymentType? =
            entries.find { it.zpayValue == value }
    }
}

/**
 * 订单状态.
 */
enum class OrderStatus {
    /** 已创建, 等待支付. */
    PENDING,

    /** 支付成功. */
    PAID,

    /** 订单超时. */
    EXPIRED,

    /** 支付失败或取消. */
    FAILED,
}

/**
 * 一笔支付订单.
 *
 * 每笔订单对应一个游戏内控制台指令, 支付成功后在服务端执行.
 */
data class PaymentOrder(
    /** 商户订单号 (由我方生成, 最多 32 位). */
    val outTradeNo: String,
    /** Z-PAY 系统订单号 (创建订单后由 Z-PAY 返回). */
    val tradeNo: String?,
    /** 发起支付的玩家 UUID. */
    val playerId: UUID,
    /** 发起支付的玩家名. */
    val playerName: String,
    /** 商品名称 (展示给 Z-PAY). */
    val productName: String,
    /** 金额 (单位: 元, 最多两位小数). */
    val amount: String,
    /** 支付方式. */
    val paymentType: PaymentType,
    /** 支付成功后要执行的控制台指令. */
    val command: String,
    /** 当前订单状态. */
    val status: OrderStatus,
    /** 二维码链接 (供玩家扫码). */
    val qrcodeUrl: String?,
    /** 二维码图片直链. */
    val qrcodeImgUrl: String?,
    /** 支付跳转 URL. */
    val payUrl: String?,
    /** 创建时间. */
    val createdAt: Instant,
    /** 支付完成时间. */
    val paidAt: Instant?,
)


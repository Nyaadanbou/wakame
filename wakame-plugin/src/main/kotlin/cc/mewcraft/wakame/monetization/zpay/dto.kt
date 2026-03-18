package cc.mewcraft.wakame.monetization.zpay

import cc.mewcraft.wakame.monetization.PaymentType

// ============================================================
//  Z-PAY mapi.php - 创建订单
// ============================================================

/**
 * 创建订单请求参数.
 *
 * 对应 Z-PAY `mapi.php` 接口.
 */
data class CreateOrderRequest(
    /** 商户订单号 (最多 32 位, 不可重复). */
    val outTradeNo: String,
    /** 商品名称. */
    val name: String,
    /** 金额 (单位: 元, 最多两位小数). */
    val money: String,
    /** 支付方式. */
    val type: PaymentType,
    /** 用户 IP 地址. */
    val clientIp: String,
    /** 异步回调地址. */
    val notifyUrl: String,
    /** 业务扩展参数, 回调时原样返回. */
    val param: String = "",
)

/**
 * 创建订单响应.
 */
data class CreateOrderResponse(
    /** 状态码, `1` 为成功. */
    val code: Int,
    /** 错误信息 (失败时). */
    val msg: String?,
    /** 支付订单号. */
    val tradeNo: String?,
    /** Z-PAY 内部订单号. */
    val oId: String?,
    /** 支付跳转 URL (若存在则直接跳转). */
    val payUrl: String?,
    /** 二维码链接 (用于生成二维码). */
    val qrcode: String?,
    /** 二维码图片直链. */
    val img: String?,
) {
    val isSuccess: Boolean get() = code == 1
}

// ============================================================
//  Z-PAY api.php?act=order - 查询订单
// ============================================================

/**
 * 查询订单响应.
 */
data class QueryOrderResponse(
    /** 状态码, `1` 为成功. */
    val code: Int,
    val msg: String?,
    val tradeNo: String?,
    val outTradeNo: String?,
    val type: String?,
    val pid: String?,
    val addtime: String?,
    val endtime: String?,
    val name: String?,
    val money: String?,
    /** `1` 为已支付, `0` 为未支付. */
    val status: Int?,
    val param: String?,
    val buyer: String?,
) {
    val isSuccess: Boolean get() = code == 1
    val isPaid: Boolean get() = status == 1
}

// ============================================================
//  Z-PAY api.php?act=refund - 退款
// ============================================================

/**
 * 退款响应.
 */
data class RefundResponse(
    /** 状态码, `1` 为成功. */
    val code: Int,
    val msg: String?,
) {
    val isSuccess: Boolean get() = code == 1
}

// ============================================================
//  Z-PAY 异步回调通知
// ============================================================

/**
 * Z-PAY 支付结果异步通知参数.
 *
 * 对应回调 `notify_url` 收到的数据.
 */
data class ZPayNotification(
    val pid: String,
    val tradeNo: String,
    val outTradeNo: String,
    val type: String,
    val name: String,
    val money: String,
    val tradeStatus: String,
    val param: String,
    val sign: String,
    val signType: String,
) {
    /** 是否支付成功. */
    val isTradeSuccess: Boolean get() = tradeStatus == "TRADE_SUCCESS"

    /**
     * 提取参与签名验证的参数 (不含 sign, sign_type, 空值).
     */
    fun toSignParams(): Map<String, String> = buildMap {
        put("pid", pid)
        put("trade_no", tradeNo)
        put("out_trade_no", outTradeNo)
        put("type", type)
        put("name", name)
        put("money", money)
        put("trade_status", tradeStatus)
        if (param.isNotEmpty()) put("param", param)
    }
}

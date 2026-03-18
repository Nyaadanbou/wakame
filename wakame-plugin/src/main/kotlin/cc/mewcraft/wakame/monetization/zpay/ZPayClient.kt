package cc.mewcraft.wakame.monetization.zpay

/**
 * Z-PAY HTTP 客户端接口.
 *
 * 封装了与 Z-PAY 支付平台的所有 HTTP 通讯, 包括创建订单, 查询订单, 退款等操作.
 */
interface ZPayClient {

    /**
     * 通过 Z-PAY API 接口创建支付订单.
     *
     * 对应 `mapi.php`, 返回二维码/支付链接等信息.
     *
     * @param request 创建订单的请求参数
     * @return Z-PAY 的响应, 包含二维码链接等
     * @throws ZPayApiException 当 Z-PAY 返回错误或网络异常时
     */
    suspend fun createOrder(request: CreateOrderRequest): CreateOrderResponse

    /**
     * 查询单个订单的支付状态.
     *
     * 对应 `api.php?act=order`.
     *
     * @param outTradeNo 商户订单号
     * @return 订单详情
     * @throws ZPayApiException 当 Z-PAY 返回错误或网络异常时
     */
    suspend fun queryOrder(outTradeNo: String): QueryOrderResponse

    /**
     * 查询单个订单的支付状态 (通过 Z-PAY 系统订单号).
     *
     * @param tradeNo Z-PAY 系统订单号
     * @return 订单详情
     * @throws ZPayApiException 当 Z-PAY 返回错误或网络异常时
     */
    suspend fun queryOrderByTradeNo(tradeNo: String): QueryOrderResponse

    /**
     * 提交订单退款.
     *
     * 对应 `api.php?act=refund`.
     *
     * @param outTradeNo 商户订单号
     * @param money 退款金额 (大多数通道需要与原订单金额一致)
     * @return 退款结果
     * @throws ZPayApiException 当 Z-PAY 返回错误或网络异常时
     */
    suspend fun refundOrder(outTradeNo: String, money: String): RefundResponse
}

/**
 * Z-PAY API 调用异常.
 */
class ZPayApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

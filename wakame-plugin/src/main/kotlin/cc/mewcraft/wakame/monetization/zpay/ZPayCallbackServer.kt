package cc.mewcraft.wakame.monetization.zpay

/**
 * Z-PAY 异步回调接收服务器接口.
 *
 * 内嵌一个 Ktor HTTP 服务器, 用于接收 Z-PAY 的支付结果异步通知.
 * Z-PAY 会向配置中的 `notify_url` 发送 GET 请求来通知支付结果.
 *
 * ### 生命周期
 *
 * - 在插件启动时 ([start]) 开始监听.
 * - 在插件关闭时 ([stop]) 停止监听, 释放端口.
 *
 * ### 回调处理流程
 *
 * 1. 接收到 Z-PAY 的 GET 请求, 解析参数为 [ZPayNotification].
 * 2. 验证签名, 校验金额.
 * 3. 委托给 [PaymentCallbackHandler] 执行业务逻辑 (更新订单状态, 执行指令等).
 * 4. 成功处理后返回纯文本 `success`, 否则返回 `fail`.
 */
interface ZPayCallbackServer {

    /**
     * 启动回调服务器, 开始监听指定端口.
     */
    fun start()

    /**
     * 停止回调服务器, 释放资源.
     */
    fun stop()
}

/**
 * 支付回调业务处理器.
 *
 * 由 [ZPayCallbackServer] 在收到合法回调后调用, 负责具体的业务逻辑 (更新订单, 发放商品, 执行指令等).
 */
interface PaymentCallbackHandler {

    /**
     * 处理一笔成功的支付通知.
     *
     * 实现应保证幂等性 (同一笔订单多次通知不会重复发放).
     *
     * @param notification 经过签名验证的回调通知数据
     * @return 是否处理成功
     */
    suspend fun onPaymentSuccess(notification: ZPayNotification): Boolean
}

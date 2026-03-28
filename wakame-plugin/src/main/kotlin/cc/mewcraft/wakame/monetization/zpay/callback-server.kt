package cc.mewcraft.wakame.monetization.zpay

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.monetization.CallbackServer
import cc.mewcraft.wakame.util.decorate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger

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
internal interface ZPayCallbackServer {

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
internal interface PaymentCallbackHandler {

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

/**
 * [ZPayCallbackServer] 的 Ktor/CIO 实现.
 *
 * 启动一个内嵌的 CIO HTTP 服务器,
 * 监听 Z-PAY 的异步支付结果通知 (GET 请求).
 *
 * @param config 回调服务器配置 (host, port)
 * @param signature 签名验证工具
 * @param handler 业务回调处理器
 */
internal class ZPayCallbackServerImpl(
    private val config: CallbackServer,
    private val signature: ZPaySignature,
    private val handler: PaymentCallbackHandler,
) : ZPayCallbackServer {

    private val logger: Logger = LOGGER.decorate(this::class)
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

    override fun start() {
        server = embeddedServer(CIO, host = config.host, port = config.port) {
            routing {
                get("/zpay/notify") {
                    handleNotify(call)
                }
            }
        }.also {
            // start(wait = false) 以非阻塞方式启动
            it.start(wait = false)
        }

        logger.info("Callback server started on ${config.host}:${config.port}")
    }

    override fun stop() {
        server?.stop(gracePeriodMillis = 1000, timeoutMillis = 3000)
        server = null
        logger.info("Callback server stopped.")
    }

    /**
     * 处理 Z-PAY GET 回调.
     *
     * 流程:
     * 1. 从 query parameters 解析出 [ZPayNotification].
     * 2. 验证签名.
     * 3. 检查 trade_status == TRADE_SUCCESS.
     * 4. 委托给 [PaymentCallbackHandler.onPaymentSuccess].
     * 5. 成功返回纯文本 `success`, 失败返回 `fail`.
     */
    private suspend fun handleNotify(call: ApplicationCall) {
        val params = call.request.queryParameters

        // 解析回调参数
        val notification = try {
            ZPayNotification(
                pid = params.require("pid"),
                tradeNo = params.require("trade_no"),
                outTradeNo = params.require("out_trade_no"),
                type = params.require("type"),
                name = params.require("name"),
                money = params.require("money"),
                tradeStatus = params.require("trade_status"),
                param = params["param"] ?: "",
                sign = params.require("sign"),
                signType = params["sign_type"] ?: "MD5",
            )
        } catch (e: MissingParamException) {
            logger.warn("Callback missing parameter: ${e.message}")
            call.respondText("fail", ContentType.Text.Plain)
            return
        }

        // 验证签名
        if (!signature.verifySign(notification.toSignParams(), notification.sign)) {
            logger.warn("Callback signature mismatch for order: ${notification.outTradeNo}")
            call.respondText("fail", ContentType.Text.Plain)
            return
        }

        // 非成功状态, 直接应答 (不做业务处理)
        if (!notification.isTradeSuccess) {
            logger.info("Callback received non-success status: ${notification.tradeStatus} for order: ${notification.outTradeNo}")
            call.respondText("success", ContentType.Text.Plain)
            return
        }

        // 委托业务处理
        val handled = try {
            handler.onPaymentSuccess(notification)
        } catch (e: Exception) {
            logger.error("Error handling payment callback for order: ${notification.outTradeNo}", e)
            false
        }

        if (handled) {
            call.respondText("success", ContentType.Text.Plain)
        } else {
            call.respondText("fail", ContentType.Text.Plain)
        }
    }

    /**
     * 从 query parameters 中取必填参数, 不存在时抛出异常.
     */
    private fun Parameters.require(name: String): String {
        return this[name] ?: throw MissingParamException(name)
    }

    private class MissingParamException(param: String) : Exception(param)
}

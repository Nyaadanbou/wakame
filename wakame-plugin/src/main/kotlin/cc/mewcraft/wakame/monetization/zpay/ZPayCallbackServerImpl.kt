package cc.mewcraft.wakame.monetization.zpay

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.monetization.CallbackServer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
class ZPayCallbackServerImpl(
    private val config: CallbackServer,
    private val signature: ZPaySignature,
    private val handler: PaymentCallbackHandler,
) : ZPayCallbackServer {

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

        LOGGER.info("[Monetization] Callback server started on ${config.host}:${config.port}")
    }

    override fun stop() {
        server?.stop(gracePeriodMillis = 1000, timeoutMillis = 3000)
        server = null
        LOGGER.info("[Monetization] Callback server stopped.")
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
            LOGGER.warn("[Monetization] Callback missing parameter: ${e.message}")
            call.respondText("fail", ContentType.Text.Plain)
            return
        }

        // 验证签名
        if (!signature.verifySign(notification.toSignParams(), notification.sign)) {
            LOGGER.warn("[Monetization] Callback signature mismatch for order: ${notification.outTradeNo}")
            call.respondText("fail", ContentType.Text.Plain)
            return
        }

        // 非成功状态, 直接应答 (不做业务处理)
        if (!notification.isTradeSuccess) {
            LOGGER.info("[Monetization] Callback received non-success status: ${notification.tradeStatus} for order: ${notification.outTradeNo}")
            call.respondText("success", ContentType.Text.Plain)
            return
        }

        // 委托业务处理
        val handled = try {
            handler.onPaymentSuccess(notification)
        } catch (e: Exception) {
            LOGGER.error("[Monetization] Error handling payment callback for order: ${notification.outTradeNo}", e)
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


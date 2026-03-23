package cc.mewcraft.wakame.monetization.zpay

import cc.mewcraft.wakame.monetization.ZPayApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Z-PAY HTTP 客户端接口.
 *
 * 封装了与 Z-PAY 支付平台的所有 HTTP 通讯, 包括创建订单, 查询订单, 退款等操作.
 */
internal interface ZPayClient {

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
internal class ZPayApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * [ZPayClient] 的 Ktor 实现.
 *
 * 使用 ktor-client-java 作为 HTTP 引擎.
 *
 * @param config Z-PAY API 配置 (url, pid, pkey)
 * @param signature 签名工具
 */
internal class ZPayClientImpl(
    private val config: ZPayApi,
    private val signature: ZPaySignature,
) : ZPayClient {

    private val httpClient = HttpClient(Java)

    // --------------------------------------------------
    //  mapi.php - 创建订单
    // --------------------------------------------------

    override suspend fun createOrder(request: CreateOrderRequest): CreateOrderResponse {
        // 组装参与签名的参数
        val signParams = buildMap {
            put("pid", config.pid)
            put("type", request.type.zpayValue)
            put("out_trade_no", request.outTradeNo)
            put("notify_url", request.notifyUrl)
            put("name", request.name)
            put("money", request.money)
            put("clientip", request.clientIp)
            if (request.param.isNotEmpty()) put("param", request.param)
        }
        val sign = signature.generateSign(signParams)

        val response: HttpResponse = try {
            httpClient.submitForm(
                url = config.url.trimEnd('/') + "/mapi.php",
                formParameters = parameters {
                    signParams.forEach { (k, v) -> append(k, v) }
                    append("sign", sign)
                    append("sign_type", "MD5")
                }
            )
        } catch (e: Exception) {
            throw ZPayApiException("Failed to call mapi.php: ${e.message}", e)
        }

        val body = response.bodyAsText()
        return try {
            val obj = JsonParser.parseString(body).asJsonObject
            CreateOrderResponse(
                code = obj.intOrDefault("code", -1),
                msg = obj.stringOrNull("msg"),
                tradeNo = obj.stringOrNull("trade_no"),
                oId = obj.stringOrNull("O_id"),
                payUrl = obj.stringOrNull("payurl"),
                qrcode = obj.stringOrNull("qrcode"),
                img = obj.stringOrNull("img"),
            )
        } catch (e: Exception) {
            throw ZPayApiException("Failed to parse mapi.php response: $body", e)
        }
    }

    // --------------------------------------------------
    //  api.php?act=order - 查询订单 (商户订单号)
    // --------------------------------------------------

    override suspend fun queryOrder(outTradeNo: String): QueryOrderResponse {
        return doQueryOrder {
            parameter("out_trade_no", outTradeNo)
        }
    }

    // --------------------------------------------------
    //  api.php?act=order - 查询订单 (Z-PAY 系统订单号)
    // --------------------------------------------------

    override suspend fun queryOrderByTradeNo(tradeNo: String): QueryOrderResponse {
        return doQueryOrder {
            parameter("trade_no", tradeNo)
        }
    }

    private suspend fun doQueryOrder(block: HttpRequestBuilder.() -> Unit): QueryOrderResponse {
        val response: HttpResponse = try {
            httpClient.get(config.url.trimEnd('/') + "/api.php") {
                parameter("act", "order")
                parameter("pid", config.pid)
                parameter("key", config.pkey)
                block()
            }
        } catch (e: Exception) {
            throw ZPayApiException("Failed to call api.php?act=order: ${e.message}", e)
        }

        val body = response.bodyAsText()
        return try {
            val obj = JsonParser.parseString(body).asJsonObject
            QueryOrderResponse(
                code = obj.intOrDefault("code", -1),
                msg = obj.stringOrNull("msg"),
                tradeNo = obj.stringOrNull("trade_no"),
                outTradeNo = obj.stringOrNull("out_trade_no"),
                type = obj.stringOrNull("type"),
                pid = obj.stringOrNull("pid"),
                addtime = obj.stringOrNull("addtime"),
                endtime = obj.stringOrNull("endtime"),
                name = obj.stringOrNull("name"),
                money = obj.stringOrNull("money"),
                status = obj.intOrNull("status"),
                param = obj.stringOrNull("param"),
                buyer = obj.stringOrNull("buyer"),
            )
        } catch (e: Exception) {
            throw ZPayApiException("Failed to parse api.php?act=order response: $body", e)
        }
    }

    // --------------------------------------------------
    //  api.php?act=refund - 退款
    // --------------------------------------------------

    override suspend fun refundOrder(outTradeNo: String, money: String): RefundResponse {
        val response: HttpResponse = try {
            httpClient.submitForm(
                url = config.url.trimEnd('/') + "/api.php?act=refund",
                formParameters = parameters {
                    append("pid", config.pid)
                    append("key", config.pkey)
                    append("out_trade_no", outTradeNo)
                    append("money", money)
                }
            )
        } catch (e: Exception) {
            throw ZPayApiException("Failed to call api.php?act=refund: ${e.message}", e)
        }

        val body = response.bodyAsText()
        return try {
            val obj = JsonParser.parseString(body).asJsonObject
            RefundResponse(
                code = obj.intOrDefault("code", -1),
                msg = obj.stringOrNull("msg"),
            )
        } catch (e: Exception) {
            throw ZPayApiException("Failed to parse api.php?act=refund response: $body", e)
        }
    }

    // --------------------------------------------------
    //  Closable
    // --------------------------------------------------

    /**
     * 释放底层 Ktor HttpClient 所占用的资源.
     */
    fun close() {
        httpClient.close()
    }

    // --------------------------------------------------
    //  Gson helper extensions
    // --------------------------------------------------

    /**
     * 安全取字符串, key 不存在或非 primitive 时返回 null.
     */
    private fun JsonObject.stringOrNull(key: String): String? {
        val element = get(key) ?: return null
        return if (element is JsonPrimitive) element.asString else null
    }

    /**
     * 安全取 Int, key 不存在或非数字时返回 null.
     */
    private fun JsonObject.intOrNull(key: String): Int? {
        val element = get(key) ?: return null
        if (element is JsonPrimitive) {
            if (element.isNumber) return element.asInt
            // Z-PAY 的 code 字段可能是字符串 "error"
            return element.asString.toIntOrNull()
        }
        return null
    }

    /**
     * 安全取 Int, key 不存在或非数字时返回默认值.
     */
    private fun JsonObject.intOrDefault(key: String, default: Int): Int {
        return intOrNull(key) ?: default
    }
}

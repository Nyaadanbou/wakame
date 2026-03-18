package cc.mewcraft.wakame.monetization

import cc.mewcraft.wakame.monetization.zpay.ZPayNotification
import java.util.*

/**
 * 支付服务主接口.
 *
 * 协调 [OrderRepository], [cc.mewcraft.wakame.monetization.zpay.ZPayClient], [cc.mewcraft.wakame.monetization.zpay.ZPayCallbackServer] 等组件, 提供面向游戏逻辑的高层支付操作.
 *
 * ### 典型支付流程
 *
 * 1. 玩家在游戏内发起支付 → [createPayment]
 * 2. 服务端调用 Z-PAY 创建订单, 获取二维码链接
 * 3. 游戏内向玩家展示二维码 (地图发包, 由前端负责)
 * 4. 玩家扫码支付
 * 5. Z-PAY 异步回调 → [handleCallback]
 * 6. 验证通过后更新订单状态, 在控制台执行预设指令
 */
interface PaymentService {

    /**
     * 为指定玩家创建一笔支付订单.
     *
     * @param playerId 玩家 UUID
     * @param playerName 玩家名 (用于指令模板替换)
     * @param productName 商品名称 (展示给支付平台)
     * @param amount 金额 (元, 最多两位小数)
     * @param paymentType 支付方式
     * @param command 支付成功后执行的控制台指令
     * @return 创建好的订单 (包含二维码链接等)
     * @throws PaymentException 创建失败时
     */
    suspend fun createPayment(
        playerId: UUID,
        playerName: String,
        productName: String,
        amount: String,
        paymentType: PaymentType,
        command: String,
    ): PaymentOrder

    /**
     * 处理 Z-PAY 异步回调通知.
     *
     * 包含签名验证, 金额校验, 状态更新, 指令执行等全部流程.
     *
     * @param notification 回调通知数据
     * @return 是否处理成功 (返回 true 时应答 Z-PAY `success`)
     */
    suspend fun handleCallback(notification: ZPayNotification): Boolean

    /**
     * 主动查询订单的最新支付状态 (向 Z-PAY 发起查询).
     *
     * @param outTradeNo 商户订单号
     * @return 更新后的订单, 如果订单不存在则返回 null
     */
    suspend fun queryPayment(outTradeNo: String): PaymentOrder?

    /**
     * 取消一笔待支付的订单.
     *
     * @param outTradeNo 商户订单号
     * @return 是否取消成功
     */
    suspend fun cancelPayment(outTradeNo: String): Boolean

    /**
     * 将指定玩家的所有超时未支付订单标记为过期.
     *
     * @param playerId 玩家 UUID
     * @return 过期的订单数量
     */
    suspend fun expireTimeoutOrders(playerId: UUID): Int
}

/**
 * 支付服务异常.
 */
class PaymentException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

package cc.mewcraft.wakame.monetization

import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * 支付模块的公共入口.
 *
 * 仅暴露业务层需要的操作, 隐藏所有 Z-PAY 通讯细节和内部组件.
 * 使用方式类似 [kotlin.random.Random]:
 *
 * ```
 * // 创建支付订单
 * val order = Monetization.createPayment(playerId, playerName, "月卡", "30.00", PaymentType.WXPAY, "lp user {player} parent add vip")
 *
 * // 查询订单
 * val order = Monetization.findOrder(outTradeNo)
 *
 * // 查询玩家所有待支付订单
 * val pending = Monetization.findPendingOrders(playerId)
 * ```
 */
interface Monetization {

    //<editor-fold desc="支付操作">
    /**
     * 为指定玩家创建一笔支付订单.
     *
     * @param playerId 玩家 UUID
     * @param playerName 玩家名 (用于指令模板中的 `{player}` 替换)
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
     * 主动查询订单的最新支付状态.
     *
     * 若订单仍为 PENDING, 会向支付平台发起查询并同步状态.
     *
     * @param outTradeNo 商户订单号
     * @return 最新状态的订单, 不存在则返回 null
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
    //</editor-fold>

    //<editor-fold desc="订单查询">
    /**
     * 根据商户订单号查询订单.
     */
    suspend fun findOrder(outTradeNo: String): PaymentOrder?

    /**
     * 查询指定玩家的所有订单.
     */
    suspend fun findOrdersByPlayer(playerId: UUID): List<PaymentOrder>

    /**
     * 查询指定玩家的所有待支付订单.
     */
    suspend fun findPendingOrders(playerId: UUID): List<PaymentOrder>

    /**
     * 查询指定玩家的累计充值金额 (仅统计已支付订单).
     *
     * @return 累计金额字符串 (例如 "130.00"), 无充值记录时返回 "0"
     */
    suspend fun getTotalPaidAmount(playerId: UUID): String

    /**
     * 查询指定玩家的已支付订单数.
     */
    suspend fun getPaidOrderCount(playerId: UUID): Int
    //</editor-fold>

    companion object Default : Monetization {
        private var implementation: Monetization = NoOp

        internal fun setImplementation(impl: Monetization) {
            this.implementation = impl
        }

        internal fun clearImplementation() {
            this.implementation = NoOp
        }

        override suspend fun createPayment(
            playerId: UUID,
            playerName: String,
            productName: String,
            amount: String,
            paymentType: PaymentType,
            command: String,
        ): PaymentOrder =
            implementation.createPayment(playerId, playerName, productName, amount, paymentType, command)

        override suspend fun queryPayment(outTradeNo: String): PaymentOrder? =
            implementation.queryPayment(outTradeNo)

        override suspend fun cancelPayment(outTradeNo: String): Boolean =
            implementation.cancelPayment(outTradeNo)

        override suspend fun expireTimeoutOrders(playerId: UUID): Int =
            implementation.expireTimeoutOrders(playerId)

        override suspend fun findOrder(outTradeNo: String): PaymentOrder? =
            implementation.findOrder(outTradeNo)

        override suspend fun findOrdersByPlayer(playerId: UUID): List<PaymentOrder> =
            implementation.findOrdersByPlayer(playerId)

        override suspend fun findPendingOrders(playerId: UUID): List<PaymentOrder> =
            implementation.findPendingOrders(playerId)

        override suspend fun getTotalPaidAmount(playerId: UUID): String =
            implementation.getTotalPaidAmount(playerId)

        override suspend fun getPaidOrderCount(playerId: UUID): Int =
            implementation.getPaidOrderCount(playerId)
    }

    /**
     * 空操作实现: 模块未启用时的默认行为, 所有操作静默返回安全默认值.
     */
    private object NoOp : Monetization {
        override suspend fun createPayment(
            playerId: UUID, playerName: String,
            productName: String, amount: String,
            paymentType: PaymentType, command: String,
        ): PaymentOrder = PaymentOrder(
            outTradeNo = "", tradeNo = null,
            playerId = playerId, playerName = playerName,
            productName = productName, amount = amount,
            paymentType = paymentType, command = command,
            status = OrderStatus.FAILED, qrcodeUrl = null,
            qrcodeImgUrl = null, payUrl = null,
            createdAt = Instant.EPOCH, paidAt = null,
        )

        override suspend fun queryPayment(outTradeNo: String): PaymentOrder? = null
        override suspend fun cancelPayment(outTradeNo: String): Boolean = false
        override suspend fun expireTimeoutOrders(playerId: UUID): Int = 0
        override suspend fun findOrder(outTradeNo: String): PaymentOrder? = null
        override suspend fun findOrdersByPlayer(playerId: UUID): List<PaymentOrder> = emptyList()
        override suspend fun findPendingOrders(playerId: UUID): List<PaymentOrder> = emptyList()
        override suspend fun getTotalPaidAmount(playerId: UUID): String = "0"
        override suspend fun getPaidOrderCount(playerId: UUID): Int = 0
    }
}

/**
 * [Monetization] 的内部实现, 桥接 [PaymentService] 和 [OrderRepository].
 *
 * @param service 支付服务. 仅在 Z-PAY 通讯启用的服务器上非空;
 *                其他服务器上为 `null`, 此时仅支持订单查询, 不支持创建/主动查询支付.
 * @param repository 订单仓库. 在所有启用了 monetization 的服务器上都可用.
 */
internal class MonetizationImpl(
    private val service: PaymentService?,
    private val repository: OrderRepository,
) : Monetization {

    override suspend fun createPayment(
        playerId: UUID, playerName: String, productName: String,
        amount: String, paymentType: PaymentType, command: String,
    ): PaymentOrder {
        val svc = service ?: throw PaymentException("Payment service is not available on this server. Orders can only be created on the designated payment server.")
        return svc.createPayment(playerId, playerName, productName, amount, paymentType, command)
    }

    override suspend fun queryPayment(outTradeNo: String): PaymentOrder? {
        // 有支付服务时, 向 Z-PAY 发起主动查询并同步状态
        if (service != null) return service.queryPayment(outTradeNo)
        // 无支付服务时, 仅返回本地数据库中的订单快照
        return repository.findByOutTradeNo(outTradeNo)
    }

    override suspend fun cancelPayment(outTradeNo: String): Boolean {
        val svc = service ?: throw PaymentException("Payment service is not available on this server. Orders can only be cancelled on the designated payment server.")
        return svc.cancelPayment(outTradeNo)
    }

    override suspend fun expireTimeoutOrders(playerId: UUID): Int {
        val svc = service ?: throw PaymentException("Payment service is not available on this server. Orders can only be expired on the designated payment server.")
        return svc.expireTimeoutOrders(playerId)
    }

    // ---- 订单查询: 委托给 OrderRepository ----

    override suspend fun findOrder(outTradeNo: String): PaymentOrder? =
        repository.findByOutTradeNo(outTradeNo)

    override suspend fun findOrdersByPlayer(playerId: UUID): List<PaymentOrder> =
        repository.findByPlayer(playerId)

    override suspend fun findPendingOrders(playerId: UUID): List<PaymentOrder> =
        repository.findPendingByPlayer(playerId)

    override suspend fun getTotalPaidAmount(playerId: UUID): String {
        return repository.findByPlayer(playerId)
            .filter { it.status == OrderStatus.PAID }
            .fold(BigDecimal.ZERO) { acc, order ->
                acc + (order.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO)
            }
            .stripTrailingZeros()
            .toPlainString()
    }

    override suspend fun getPaidOrderCount(playerId: UUID): Int {
        return repository.findByPlayer(playerId)
            .count { it.status == OrderStatus.PAID }
    }
}

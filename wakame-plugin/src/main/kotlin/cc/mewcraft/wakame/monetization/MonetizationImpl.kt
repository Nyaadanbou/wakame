package cc.mewcraft.wakame.monetization

import java.math.BigDecimal
import java.util.*

/**
 * [Monetization] 的内部实现, 桥接 [PaymentService] 和 [OrderRepository].
 */
internal class MonetizationImpl(
    private val service: PaymentService,
    private val repository: OrderRepository,
) : Monetization {


    override suspend fun createPayment(
        playerId: UUID, playerName: String, productName: String,
        amount: String, paymentType: PaymentType, command: String,
    ): PaymentOrder = service.createPayment(playerId, playerName, productName, amount, paymentType, command)

    override suspend fun queryPayment(outTradeNo: String): PaymentOrder? =
        service.queryPayment(outTradeNo)

    override suspend fun cancelPayment(outTradeNo: String): Boolean =
        service.cancelPayment(outTradeNo)

    override suspend fun expireTimeoutOrders(playerId: UUID): Int =
        service.expireTimeoutOrders(playerId)

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

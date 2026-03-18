package cc.mewcraft.wakame.monetization

import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 订单持久化接口.
 *
 * 第一版使用 [InMemoryOrderRepository] 实现, 后续迭代会替换为数据库持久化实现.
 */
interface OrderRepository {

    /**
     * 保存一笔新订单.
     *
     * 如果 [PaymentOrder.outTradeNo] 已存在, 则覆盖.
     */
    suspend fun save(order: PaymentOrder)

    /**
     * 根据商户订单号查询.
     */
    suspend fun findByOutTradeNo(outTradeNo: String): PaymentOrder?

    /**
     * 根据 Z-PAY 系统订单号查询.
     */
    suspend fun findByTradeNo(tradeNo: String): PaymentOrder?

    /**
     * 查询指定玩家的所有订单.
     */
    suspend fun findByPlayer(playerId: UUID): List<PaymentOrder>

    /**
     * 查询指定玩家的所有待支付订单.
     */
    suspend fun findPendingByPlayer(playerId: UUID): List<PaymentOrder>

    /**
     * 更新订单状态.
     *
     * @return 是否更新成功 (订单存在且状态变更)
     */
    suspend fun updateStatus(outTradeNo: String, status: OrderStatus, paidAt: Instant? = null): Boolean

    /**
     * 更新 Z-PAY 返回的订单号.
     */
    suspend fun updateTradeNo(outTradeNo: String, tradeNo: String): Boolean

    /**
     * 删除指定时间之前创建的已过期/已完成订单.
     *
     * @return 删除的订单数量
     */
    suspend fun deleteBefore(before: Instant): Int

    /**
     * 删除指定订单.
     */
    suspend fun delete(outTradeNo: String): Boolean
}

/**
 * 基于内存的 [OrderRepository] 实现.
 *
 * 服务器重启后数据会丢失. 仅用于开发/测试阶段.
 */
class InMemoryOrderRepository : OrderRepository {

    // OutTradeNo -> PaymentOrder
    private val orders = ConcurrentHashMap<String, PaymentOrder>()

    override suspend fun save(order: PaymentOrder) {
        orders[order.outTradeNo] = order
    }

    override suspend fun findByOutTradeNo(outTradeNo: String): PaymentOrder? {
        return orders[outTradeNo]
    }

    override suspend fun findByTradeNo(tradeNo: String): PaymentOrder? {
        return orders.values.find { it.tradeNo == tradeNo }
    }

    override suspend fun findByPlayer(playerId: UUID): List<PaymentOrder> {
        return orders.values.filter { it.playerId == playerId }
    }

    override suspend fun findPendingByPlayer(playerId: UUID): List<PaymentOrder> {
        return orders.values.filter {
            it.playerId == playerId && it.status == OrderStatus.PENDING
        }
    }

    override suspend fun updateStatus(outTradeNo: String, status: OrderStatus, paidAt: Instant?): Boolean {
        val existing = orders[outTradeNo] ?: return false
        orders[outTradeNo] = existing.copy(status = status, paidAt = paidAt)
        return true
    }

    override suspend fun updateTradeNo(outTradeNo: String, tradeNo: String): Boolean {
        val existing = orders[outTradeNo] ?: return false
        orders[outTradeNo] = existing.copy(tradeNo = tradeNo)
        return true
    }

    override suspend fun deleteBefore(before: Instant): Int {
        val toRemove = orders.values.filter {
            it.createdAt.isBefore(before) && it.status != OrderStatus.PENDING
        }
        toRemove.forEach { orders.remove(it.outTradeNo) }
        return toRemove.size
    }

    override suspend fun delete(outTradeNo: String): Boolean {
        return orders.remove(outTradeNo) != null
    }
}

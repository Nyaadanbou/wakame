package cc.mewcraft.wakame.monetization

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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

//<editor-fold desc="In-Memory Implementation">
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
//</editor-fold>

//<editor-fold desc="Exposed Implementation">
/**
 * 订单数据库表定义.
 *
 * 表名 `koish_payment_orders`, 以商户订单号 (`out_trade_no`) 作为主键.
 * 时间字段使用 epoch seconds (`Long`) 存储, 避免跨数据库方言的时区兼容问题.
 */
object PaymentOrdersTable : Table("koish_payment_orders") {
    val outTradeNo = varchar("out_trade_no", 32)
    val tradeNo = varchar("trade_no", 64).nullable()
    val playerId = varchar("player_id", 36) // UUID string
    val playerName = varchar("player_name", 32)
    val productName = varchar("product_name", 100)
    val amount = varchar("amount", 16)
    val paymentType = varchar("payment_type", 16)
    val command = text("command")
    val status = varchar("status", 16)
    val qrcodeUrl = text("qrcode_url").nullable()
    val qrcodeImgUrl = text("qrcode_img_url").nullable()
    val payUrl = text("pay_url").nullable()
    val createdAt = long("created_at") // epoch seconds
    val paidAt = long("paid_at").nullable() // epoch seconds

    override val primaryKey = PrimaryKey(outTradeNo)
}

/**
 * 基于 Exposed 的 [OrderRepository] 数据库持久化实现.
 *
 * 使用提供的 Exposed [Database] 实例进行数据访问.
 * 订单数据持久化到 [PaymentOrdersTable] 表中, 服务器重启后数据不会丢失.
 *
 * @param db 用于该仓库的 Exposed [Database] 实例
 */
class ExposedOrderRepository(
    private val db: Database,
) : OrderRepository {

    /**
     * 初始化表结构. 应在模块启动时调用一次.
     *
     * 如果表已存在则不做任何操作, 如果不存在则自动创建.
     */
    fun createSchemaIfNeeded() {
        transaction(db) {
            SchemaUtils.create(PaymentOrdersTable)
        }
    }

    override suspend fun save(order: PaymentOrder) {
        transaction(db) {
            PaymentOrdersTable.upsert {
                it[outTradeNo] = order.outTradeNo
                it[tradeNo] = order.tradeNo
                it[playerId] = order.playerId.toString()
                it[playerName] = order.playerName
                it[productName] = order.productName
                it[amount] = order.amount
                it[paymentType] = order.paymentType.name
                it[command] = order.command
                it[status] = order.status.name
                it[qrcodeUrl] = order.qrcodeUrl
                it[qrcodeImgUrl] = order.qrcodeImgUrl
                it[payUrl] = order.payUrl
                it[createdAt] = order.createdAt.epochSecond
                it[paidAt] = order.paidAt?.epochSecond
            }
        }
    }

    override suspend fun findByOutTradeNo(outTradeNo: String): PaymentOrder? {
        return transaction(db) {
            PaymentOrdersTable
                .selectAll()
                .where { PaymentOrdersTable.outTradeNo eq outTradeNo }
                .singleOrNull()
                ?.toPaymentOrder()
        }
    }

    override suspend fun findByTradeNo(tradeNo: String): PaymentOrder? {
        return transaction(db) {
            PaymentOrdersTable
                .selectAll()
                .where { PaymentOrdersTable.tradeNo eq tradeNo }
                .singleOrNull()
                ?.toPaymentOrder()
        }
    }

    override suspend fun findByPlayer(playerId: UUID): List<PaymentOrder> {
        return transaction(db) {
            PaymentOrdersTable
                .selectAll()
                .where { PaymentOrdersTable.playerId eq playerId.toString() }
                .map { it.toPaymentOrder() }
        }
    }

    override suspend fun findPendingByPlayer(playerId: UUID): List<PaymentOrder> {
        return transaction(db) {
            PaymentOrdersTable
                .selectAll()
                .where {
                    (PaymentOrdersTable.playerId eq playerId.toString()) and
                            (PaymentOrdersTable.status eq OrderStatus.PENDING.name)
                }
                .map { it.toPaymentOrder() }
        }
    }

    override suspend fun updateStatus(outTradeNo: String, status: OrderStatus, paidAt: Instant?): Boolean {
        return transaction(db) {
            PaymentOrdersTable.update({ PaymentOrdersTable.outTradeNo eq outTradeNo }) {
                it[PaymentOrdersTable.status] = status.name
                if (paidAt != null) {
                    it[PaymentOrdersTable.paidAt] = paidAt.epochSecond
                }
            } > 0
        }
    }

    override suspend fun updateTradeNo(outTradeNo: String, tradeNo: String): Boolean {
        return transaction(db) {
            PaymentOrdersTable.update({ PaymentOrdersTable.outTradeNo eq outTradeNo }) {
                it[PaymentOrdersTable.tradeNo] = tradeNo
            } > 0
        }
    }

    override suspend fun deleteBefore(before: Instant): Int {
        return transaction(db) {
            PaymentOrdersTable.deleteWhere {
                (createdAt less before.epochSecond) and
                        (status neq OrderStatus.PENDING.name)
            }
        }
    }

    override suspend fun delete(outTradeNo: String): Boolean {
        return transaction(db) {
            PaymentOrdersTable.deleteWhere {
                PaymentOrdersTable.outTradeNo eq outTradeNo
            } > 0
        }
    }

    // ================================================================
    //  Row Mapping
    // ================================================================

    /**
     * 将数据库行映射为 [PaymentOrder] 领域对象.
     */
    private fun ResultRow.toPaymentOrder(): PaymentOrder {
        return PaymentOrder(
            outTradeNo = this[PaymentOrdersTable.outTradeNo],
            tradeNo = this[PaymentOrdersTable.tradeNo],
            playerId = UUID.fromString(this[PaymentOrdersTable.playerId]),
            playerName = this[PaymentOrdersTable.playerName],
            productName = this[PaymentOrdersTable.productName],
            amount = this[PaymentOrdersTable.amount],
            paymentType = PaymentType.valueOf(this[PaymentOrdersTable.paymentType]),
            command = this[PaymentOrdersTable.command],
            status = OrderStatus.valueOf(this[PaymentOrdersTable.status]),
            qrcodeUrl = this[PaymentOrdersTable.qrcodeUrl],
            qrcodeImgUrl = this[PaymentOrdersTable.qrcodeImgUrl],
            payUrl = this[PaymentOrdersTable.payUrl],
            createdAt = Instant.ofEpochSecond(this[PaymentOrdersTable.createdAt]),
            paidAt = this[PaymentOrdersTable.paidAt]?.let { Instant.ofEpochSecond(it) },
        )
    }
}
//</editor-fold>

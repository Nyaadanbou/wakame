package cc.mewcraft.wakame.monetization

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.monetization.zpay.*
import cc.mewcraft.wakame.util.runTask
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

/**
 * [PaymentService] 的默认实现.
 *
 * 同时实现 [PaymentCallbackHandler], 作为 [ZPayCallbackServer] 的回调处理器.
 *
 * @param client Z-PAY HTTP 客户端
 * @param repository 订单仓库
 */
internal class PaymentServiceImpl(
    private val client: ZPayClient,
    private val repository: OrderRepository,
) : PaymentService, PaymentCallbackHandler {

    /**
     * 订单处理互斥锁, 防止同一订单并发回调导致重复发放.
     *
     * key = outTradeNo
     */
    private val orderLocks = ConcurrentHashMap<String, Mutex>()

    private fun lockFor(outTradeNo: String): Mutex {
        return orderLocks.getOrPut(outTradeNo) { Mutex() }
    }

    // ================================================================
    //  PaymentService - 创建订单
    // ================================================================

    override suspend fun createPayment(
        playerId: UUID,
        playerName: String,
        productName: String,
        amount: String,
        paymentType: PaymentType,
        command: String,
    ): PaymentOrder {
        // 检查待支付订单数量限制
        val pending = repository.findPendingByPlayer(playerId)
        val maxPending = MonetizationConfig.order.maxPendingPerPlayer
        if (pending.size >= maxPending) {
            throw PaymentException("Player $playerName already has $maxPending pending order(s). Please complete or cancel them first.")
        }

        // 生成商户订单号 (时间戳 + 玩家UUID后8位, 保证 ≤32 位)
        val outTradeNo = generateOutTradeNo(playerId)

        // 调用 Z-PAY 创建订单
        val notifyUrl = MonetizationConfig.callbackServer.notifyUrl
        val request = CreateOrderRequest(
            outTradeNo = outTradeNo,
            name = productName,
            money = amount,
            type = paymentType,
            clientIp = "127.0.0.1", // 游戏服务器发起, 使用服务端 IP
            notifyUrl = notifyUrl,
            param = playerId.toString(), // 通过 param 传递玩家 UUID, 回调时原样返回
        )

        val response = try {
            client.createOrder(request)
        } catch (e: ZPayApiException) {
            throw PaymentException("Failed to create order via Z-PAY: ${e.message}", e)
        }

        if (!response.isSuccess) {
            throw PaymentException("Z-PAY rejected order creation: ${response.msg}")
        }

        // 组装本地订单
        val order = PaymentOrder(
            outTradeNo = outTradeNo,
            tradeNo = response.tradeNo,
            playerId = playerId,
            playerName = playerName,
            productName = productName,
            amount = amount,
            paymentType = paymentType,
            command = command,
            status = OrderStatus.PENDING,
            qrcodeUrl = response.qrcode,
            qrcodeImgUrl = response.img,
            payUrl = response.payUrl,
            createdAt = Instant.now(),
            paidAt = null,
        )

        repository.save(order)
        LOGGER.info("[Monetization] Order created: $outTradeNo for player $playerName, amount=$amount")

        return order
    }

    // ================================================================
    //  PaymentService - 处理异步回调
    // ================================================================

    override suspend fun handleCallback(notification: ZPayNotification): Boolean {
        return onPaymentSuccess(notification)
    }

    // ================================================================
    //  PaymentCallbackHandler - 回调业务处理 (被 ZPayCallbackServer 调用)
    // ================================================================

    override suspend fun onPaymentSuccess(notification: ZPayNotification): Boolean {
        val outTradeNo = notification.outTradeNo
        val mutex = lockFor(outTradeNo)

        return mutex.withLock {
            // 1. 查找本地订单
            val order = repository.findByOutTradeNo(outTradeNo)
            if (order == null) {
                LOGGER.warn("[Monetization] Callback for unknown order: $outTradeNo")
                return@withLock false
            }

            // 2. 幂等检查: 已处理过的订单直接返回成功
            if (order.status == OrderStatus.PAID) {
                LOGGER.info("[Monetization] Duplicate callback for already paid order: $outTradeNo")
                return@withLock true
            }

            // 3. 只处理 PENDING 状态的订单
            if (order.status != OrderStatus.PENDING) {
                LOGGER.warn("[Monetization] Callback for non-pending order: $outTradeNo (status=${order.status})")
                return@withLock false
            }

            // 4. 金额校验
            if (order.amount != notification.money) {
                LOGGER.error("[Monetization] Amount mismatch for order $outTradeNo: expected=${order.amount}, got=${notification.money}")
                return@withLock false
            }

            // 5. 更新订单状态
            val now = Instant.now()
            repository.updateStatus(outTradeNo, OrderStatus.PAID, now)

            // 更新 tradeNo (如果之前没有)
            if (order.tradeNo == null && notification.tradeNo.isNotEmpty()) {
                repository.updateTradeNo(outTradeNo, notification.tradeNo)
            }

            LOGGER.info("[Monetization] Order paid: $outTradeNo, executing command for ${order.playerName}")

            // 6. 通知二维码展示: 支付已完成 (如果玩家正在查看二维码)
            QRCodeMapDisplay.notifyPaid(order.playerId)

            // 7. 使缓存失效, 让 LuckPerms/PAPI 等尽快拿到最新数据
            MonetizationCache.invalidate(order.playerId)

            // 8. 在主线程执行控制台指令
            executeCommand(order)

            // 9. 清理锁
            orderLocks.remove(outTradeNo)

            true
        }
    }

    // ================================================================
    //  PaymentService - 主动查询
    // ================================================================

    override suspend fun queryPayment(outTradeNo: String): PaymentOrder? {
        val order = repository.findByOutTradeNo(outTradeNo) ?: return null

        // 如果订单还在 PENDING, 主动向 Z-PAY 查询最新状态
        if (order.status == OrderStatus.PENDING) {
            try {
                val response = client.queryOrder(outTradeNo)
                if (response.isSuccess && response.isPaid) {
                    val now = Instant.now()
                    repository.updateStatus(outTradeNo, OrderStatus.PAID, now)
                    if (response.tradeNo != null) {
                        repository.updateTradeNo(outTradeNo, response.tradeNo)
                    }
                    LOGGER.info("[Monetization] Order $outTradeNo confirmed paid via active query, executing command for ${order.playerName}")
                    MonetizationCache.invalidate(order.playerId)
                    executeCommand(order)
                    return repository.findByOutTradeNo(outTradeNo)
                }
            } catch (e: ZPayApiException) {
                LOGGER.warn("[Monetization] Failed to query order $outTradeNo from Z-PAY: ${e.message}")
            }
        }

        return repository.findByOutTradeNo(outTradeNo)
    }

    // ================================================================
    //  PaymentService - 取消订单
    // ================================================================

    override suspend fun cancelPayment(outTradeNo: String): Boolean {
        val order = repository.findByOutTradeNo(outTradeNo) ?: return false
        if (order.status != OrderStatus.PENDING) {
            return false
        }
        repository.updateStatus(outTradeNo, OrderStatus.FAILED)
        orderLocks.remove(outTradeNo)
        LOGGER.info("[Monetization] Order cancelled: $outTradeNo")
        return true
    }

    // ================================================================
    //  PaymentService - 过期清理
    // ================================================================

    override suspend fun expireTimeoutOrders(playerId: UUID): Int {
        val timeoutSeconds = MonetizationConfig.order.timeoutSeconds
        val cutoff = Instant.now().minusSeconds(timeoutSeconds)

        val pendingOrders = repository.findPendingByPlayer(playerId)
        var count = 0
        for (order in pendingOrders) {
            if (order.createdAt.isBefore(cutoff)) {
                repository.updateStatus(order.outTradeNo, OrderStatus.EXPIRED)
                orderLocks.remove(order.outTradeNo)
                count++
            }
        }

        if (count > 0) {
            LOGGER.info("[Monetization] Expired $count timeout order(s) for player $playerId")
        }
        return count
    }

    // ================================================================
    //  Internal
    // ================================================================

    /**
     * 在服务端主线程执行控制台指令.
     *
     * 指令中的 `{player}` 占位符会被替换为玩家名.
     */
    private fun executeCommand(order: PaymentOrder) {
        val command = order.command.replace("{player}", order.playerName)
        runTask {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        }
    }

    /**
     * 生成商户订单号 (时间戳 + 玩家UUID后8位, 保证 ≤32 位)
     */
    private fun generateOutTradeNo(playerId: UUID): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        val uuidSuffix = playerId.toString().replace("-", "").takeLast(8)
        return timestamp + uuidSuffix
    }
}

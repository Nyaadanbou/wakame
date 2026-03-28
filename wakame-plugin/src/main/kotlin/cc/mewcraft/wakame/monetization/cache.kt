package cc.mewcraft.wakame.monetization

import cc.mewcraft.wakame.monetization.MonetizationCache.getPaidOrderCount
import cc.mewcraft.wakame.monetization.MonetizationCache.getTotalPaidAmount
import cc.mewcraft.wakame.monetization.MonetizationCache.invalidate
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * [Monetization] 查询结果的本地缓存.
 *
 * 提供**非挂起**的同步 API, 供 LuckPerms ContextCalculator、PlaceholderAPI 等
 * 无法使用协程的调用方使用. 内部通过带 TTL 的缓存避免频繁 [runBlocking] 阻塞.
 *
 * - 缓存命中: 直接返回, 无阻塞
 * - 缓存未命中 / 过期: [runBlocking] 查询后写入缓存
 * - 支付成功时应调用 [invalidate] 使对应玩家的缓存立即失效
 */
object MonetizationCache {

    /** 缓存过期时间 (毫秒). */
    private const val TTL_MS = 5_000L

    /** 关闭标志. 一旦为 true, 所有查询直接返回安全默认值, 不再触发 [runBlocking]. */
    @Volatile
    private var closed = false

    private class Entry<T>(val value: T, val expireAt: Long)

    private val totalPaidCache = ConcurrentHashMap<UUID, Entry<String>>()
    private val paidCountCache = ConcurrentHashMap<UUID, Entry<Int>>()

    /**
     * 获取玩家的累计充值金额 (带缓存).
     */
    fun getTotalPaidAmount(playerId: UUID): String {
        if (closed) return "0"
        val now = System.currentTimeMillis()
        totalPaidCache[playerId]?.let { if (now < it.expireAt) return it.value }
        // runCatching 兜底: 若 shutdown() 在 closed 检查之后、runBlocking 之前被调用,
        // 或底层资源 (数据库连接池等) 已关闭, 则静默返回安全默认值.
        val value = runCatching {
            runBlocking {
                Monetization.getTotalPaidAmount(playerId)
            }
        }.getOrElse {
            if (!closed) throw it; "0"
        }
        if (closed) return value // shutdown 期间不写回缓存
        totalPaidCache[playerId] = Entry(value, now + TTL_MS)
        return value
    }

    /**
     * 获取玩家的已支付订单数 (带缓存).
     */
    fun getPaidOrderCount(playerId: UUID): Int {
        if (closed) return 0
        val now = System.currentTimeMillis()
        paidCountCache[playerId]?.let { if (now < it.expireAt) return it.value }
        val value = runCatching {
            runBlocking {
                Monetization.getPaidOrderCount(playerId)
            }
        }.getOrElse {
            if (!closed) throw it; 0
        }
        if (closed) return value
        paidCountCache[playerId] = Entry(value, now + TTL_MS)
        return value
    }

    /**
     * 使指定玩家的所有缓存失效.
     *
     * 应在支付成功后调用, 使下次查询能拿到最新数据.
     */
    fun invalidate(playerId: UUID) {
        totalPaidCache.remove(playerId)
        paidCountCache.remove(playerId)
    }

    /**
     * 关闭缓存, 清除所有条目.
     *
     * 调用后, [getTotalPaidAmount] 和 [getPaidOrderCount] 将直接返回安全默认值,
     * 不再尝试 [runBlocking] 查询 (避免在关服时访问已关闭的数据库连接池).
     */
    fun shutdown() {
        closed = true
        totalPaidCache.clear()
        paidCountCache.clear()
    }
}

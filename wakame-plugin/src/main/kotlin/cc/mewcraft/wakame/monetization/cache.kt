package cc.mewcraft.wakame.monetization

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

    private class Entry<T>(val value: T, val expireAt: Long)

    private val totalPaidCache = ConcurrentHashMap<UUID, Entry<String>>()
    private val paidCountCache = ConcurrentHashMap<UUID, Entry<Int>>()

    /**
     * 获取玩家的累计充值金额 (带缓存).
     */
    fun getTotalPaidAmount(playerId: UUID): String {
        val now = System.currentTimeMillis()
        totalPaidCache[playerId]?.let { if (now < it.expireAt) return it.value }
        val value = runBlocking { Monetization.getTotalPaidAmount(playerId) }
        totalPaidCache[playerId] = Entry(value, now + TTL_MS)
        return value
    }

    /**
     * 获取玩家的已支付订单数 (带缓存).
     */
    fun getPaidOrderCount(playerId: UUID): Int {
        val now = System.currentTimeMillis()
        paidCountCache[playerId]?.let { if (now < it.expireAt) return it.value }
        val value = runBlocking { Monetization.getPaidOrderCount(playerId) }
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
}

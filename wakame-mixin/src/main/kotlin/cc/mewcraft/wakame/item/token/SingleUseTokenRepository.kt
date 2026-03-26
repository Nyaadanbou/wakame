package cc.mewcraft.wakame.item.token

import java.util.*

/**
 * 一次性令牌的仓库接口.
 *
 * 提供令牌的原子性校验与标记功能, 保证每个令牌只能被成功消耗一次.
 *
 * 该机制可用于任何需要严格防止重复消耗的物品行为, 例如兑换券等涉及真金白银的高价值消耗品.
 */
interface SingleUseTokenRepository {

    /**
     * 尝试将令牌标记为已使用.
     *
     * 使用原子操作 (INSERT ... ON CONFLICT DO NOTHING) 实现, 保证即使并发调用, 也只有一次调用返回 `true`.
     *
     * @param token 物品上的唯一令牌
     * @param playerId 使用该物品的玩家 UUID
     * @return `true` 如果标记成功 (首次使用); `false` 如果该令牌已被使用过
     */
    fun markRedeemed(token: String, playerId: UUID): Boolean

    companion object Impl : SingleUseTokenRepository {

        private var implementation: SingleUseTokenRepository = object : SingleUseTokenRepository {
            override fun markRedeemed(token: String, playerId: UUID): Boolean {
                throw NotImplementedError("SingleUseTokenRepository not initialized")
            }
        }

        fun setImplementation(impl: SingleUseTokenRepository) {
            implementation = impl
        }

        override fun markRedeemed(token: String, playerId: UUID): Boolean {
            return implementation.markRedeemed(token, playerId)
        }
    }
}

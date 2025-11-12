package cc.mewcraft.wakame.integration.playerlevel

import cc.mewcraft.wakame.SERVER
import java.util.*

/**
 * 代表一个可以提供玩家等级的东西.
 */
interface PlayerLevelIntegration {

    /**
     * 当前钩子的类型.
     * 允许在多个钩子存在的情况下, 让用户选择指定的等级系统.
     */
    val levelType: PlayerLevelType

    /**
     * Gets the player's level from the player's UUID.
     *
     * @param uuid the UUID of the player
     * @return the level of the player
     */
    fun get(uuid: UUID): Int?

    /**
     * @see getOrDefault
     */
    fun getOrDefault(uuid: UUID, def: Int): Int {
        return get(uuid) ?: def
    }

    /**
     * 该伴生类持有了 [PlayerLevelIntegration] 的当前实现.
     */
    companion object : PlayerLevelIntegration {
        private var currImpl: PlayerLevelIntegration = ZeroLevelIntegration

        fun setImplementation(impl: PlayerLevelIntegration) {
            currImpl = impl
        }

        override val levelType: PlayerLevelType
            get() = currImpl.levelType

        override fun get(uuid: UUID): Int? {
            return currImpl.get(uuid)
        }

        override fun getOrDefault(uuid: UUID, def: Int): Int {
            return get(uuid) ?: def
        }
    }
}

/**
 * A [player level integration][PlayerLevelIntegration] that always returns 0.
 * It can be used when requested implementation is not available at runtime.
 */
private object ZeroLevelIntegration : PlayerLevelIntegration {
    override val levelType: PlayerLevelType = PlayerLevelType.ZERO
    override fun get(uuid: UUID): Int = 0
}

/**
 * A [player level integration][PlayerLevelIntegration] that returns the
 * [vanilla experience level](https://minecraft.wiki/w/Experience).
 */
private object VanillaLevelIntegration : PlayerLevelIntegration {
    override val levelType: PlayerLevelType = PlayerLevelType.VANILLA
    override fun get(uuid: UUID): Int? = SERVER.getPlayer(uuid)?.level
}
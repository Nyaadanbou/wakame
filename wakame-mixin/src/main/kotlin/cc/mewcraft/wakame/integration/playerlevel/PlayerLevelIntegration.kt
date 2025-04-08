package cc.mewcraft.wakame.integration.playerlevel

import java.util.*

/**
 * 代表一个可以提供玩家等级的东西.
 */
interface PlayerLevelIntegration {

    /**
     * 当前钩子的类型.
     * 允许在多个钩子存在的情况下, 让用户选择指定的等级系统.
     */
    val type: PlayerLevelType

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

}
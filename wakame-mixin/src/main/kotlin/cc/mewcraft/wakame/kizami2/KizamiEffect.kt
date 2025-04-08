package cc.mewcraft.wakame.kizami2

import org.bukkit.entity.Player

/**
 * 铭刻的效果.
 */
interface KizamiEffect {

    /**
     * 应用铭刻效果.
     *
     * @param player 需要应用铭刻效果的玩家
     */
    fun apply(player: Player)

    /**
     * 移除铭刻效果.
     *
     * @param player 需要移除铭刻效果的玩家
     */
    fun remove(player: Player)

}
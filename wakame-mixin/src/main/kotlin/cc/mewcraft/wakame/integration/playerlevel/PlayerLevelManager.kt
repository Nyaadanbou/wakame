package cc.mewcraft.wakame.integration.playerlevel

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager.getOrDefault
import cc.mewcraft.wakame.integration.playerlevel.intrinsics.VanillaLevelIntegration
import org.jetbrains.annotations.ApiStatus
import java.util.*

object PlayerLevelManager {

    // 初始化时, 将 VanillaLevelProvider 作为默认的等级系统.
    // 如果有其他等级系统存在并且需要被使用, 该字段应该被重新赋值.
    @ApiStatus.Internal
    var integration: PlayerLevelIntegration = VanillaLevelIntegration

    /**
     * Gets the player's level from the player's UUID.
     *
     * @param uuid the UUID of the player
     * @return the level of the player
     */
    fun get(uuid: UUID): Int? {
        return integration.get(uuid)
    }

    /**
     * @see getOrDefault
     */
    fun getOrDefault(uuid: UUID, def: Int): Int {
        return get(uuid) ?: def
    }

}
// FIXME 冒险等级这个钩子非常特殊, 涉及到通过监听事件来改写用户数据加载的逻辑.
//  目前, 此文件仅仅是 PlayerLevelIntegration 的一个实现.
//  未来应该对用户数据的加载逻辑进行抽象, 以提高系统的维护性.

package cc.mewcraft.wakame.hook.impl.adventurelevel

import cc.mewcraft.adventurelevel.level.category.LevelCategory
import cc.mewcraft.adventurelevel.plugin.AdventureLevelProvider
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import java.util.UUID

/**
 * A [player level integration][PlayerLevelIntegration] that returns the
 * *adventure level* (i.e., the level from our AdventureLevel plugin).
 */
@Hook(plugins = ["AdventureLevel"])
object AdventureLevelHook : PlayerLevelIntegration {

    override val type: PlayerLevelType = PlayerLevelType.ADVENTURE

    override fun get(uuid: UUID): Int? {
        val api = AdventureLevelProvider.get()
        val userDataRepository = api.userDataRepository
        val userData = userDataRepository.getCached(uuid) ?: return null
        val primaryLevel = userData.getLevel(LevelCategory.PRIMARY)
        return primaryLevel.level
    }

}
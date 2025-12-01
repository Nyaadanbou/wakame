package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon

import cc.mewcraft.wakame.hook.impl.betonquest.util.MythicDungeonsBridge
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition

class InsideDungeon(
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        return MythicDungeonsBridge.isInsideDungeon(profile.player)
    }
}
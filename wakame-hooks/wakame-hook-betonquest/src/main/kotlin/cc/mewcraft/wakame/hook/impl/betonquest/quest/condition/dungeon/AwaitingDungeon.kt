package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon

import cc.mewcraft.wakame.hook.impl.betonquest.MythicDungeonsApi
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition

class AwaitingDungeon(
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        return MythicDungeonsApi.isAwaitingDungeon(profile.player)
    }
}
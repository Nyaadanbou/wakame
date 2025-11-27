package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon

import net.playavalon.mythicdungeons.MythicDungeons
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition

class AwaitingDungeon(
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    private val mdApi: MythicDungeons
        get() = MythicDungeons.inst()

    override fun check(profile: OnlineProfile): Boolean {
        val mdPlayer = mdApi.getMythicPlayer(profile.playerUUID) ?: return false
        return mdPlayer.isAwaitingDungeon
    }
}
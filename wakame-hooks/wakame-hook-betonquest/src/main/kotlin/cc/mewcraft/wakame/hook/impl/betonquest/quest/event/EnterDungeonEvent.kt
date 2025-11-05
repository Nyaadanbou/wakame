package cc.mewcraft.wakame.hook.impl.betonquest.quest.event

import net.playavalon.mythicdungeons.MythicDungeons
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent

class EnterDungeonEvent(
    private val dungeon: Variable<String>,
    private val logger: BetonQuestLogger,
) : OnlineEvent {

    override fun execute(profile: OnlineProfile) {
        val player = profile.player
        val dungeonName = dungeon.getValue(profile)

        if (MythicDungeons.inst().dungeonManager.get(dungeonName) == null) {
            logger.warn("No dungeon found with name '$dungeonName'")
            return
        }

        MythicDungeons.inst().sendToDungeon(player, dungeonName)
    }
}
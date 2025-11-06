package cc.mewcraft.wakame.hook.impl.betonquest.quest.event

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.integration.party.PartyIntegration
import net.playavalon.mythicdungeons.MythicDungeons
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent

class EnterDungeonEvent(
    private val dungeon: Variable<String>,
    private val useParty: Variable<Boolean>,
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

        if (useParty.getValue(profile)) {
            PartyIntegration.lookupPartyByPlayer(profile.player)
                .thenApply { party ->
                    if (party != null) {
                        party.members
                            .mapNotNull { SERVER.getPlayer(it) }
                            .filter { it.location.distance(player.location) < 8 }
                            .forEach { MythicDungeons.inst().sendToDungeon(it, dungeonName) }
                    } else {
                        logger.info("Player ${profile.player.name} is not in a party, but 'party:true' argument was used.")
                    }
                }
        }
    }
}
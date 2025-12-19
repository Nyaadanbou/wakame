package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party

import cc.mewcraft.wakame.integration.party.PartyIntegration
import cc.mewcraft.wakame.util.adventure.plain
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter


class LeavePartyEvent(
    private val logger: BetonQuestLogger,
) : OnlineEvent {

    override fun execute(profile: OnlineProfile) {
        val party = PartyIntegration.lookupPartyByPlayer(profile.playerUUID).join()
        if (party != null) {
            party.removeMember(profile.playerUUID)
            logger.info("Removed player ${profile.player.name} from party \"${party.name.plain}\"(${party.id}).")
        }
    }
}


/**
 * @param loggerFactory the logger factory to create a logger for the events
 */
class LeavePartyEventFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val logger = loggerFactory.create(LeavePartyEvent::class.java)
        val onlineEvent = LeavePartyEvent(logger)
        val questPackage = instruction.getPackage()
        return OnlineEventAdapter(onlineEvent, logger, questPackage)
    }
}
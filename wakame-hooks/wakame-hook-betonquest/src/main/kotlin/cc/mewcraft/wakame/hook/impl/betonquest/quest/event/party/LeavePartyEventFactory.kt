package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter

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
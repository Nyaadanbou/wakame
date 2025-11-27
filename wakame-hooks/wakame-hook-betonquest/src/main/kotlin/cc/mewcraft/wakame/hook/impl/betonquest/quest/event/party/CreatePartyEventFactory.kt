package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.ProfileProvider
import org.betonquest.betonquest.api.quest.QuestTypeApi
import org.betonquest.betonquest.api.quest.condition.ConditionID
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter

/**
 * @param loggerFactory the logger factory to create a logger for the events
 * @param questTypeApi the Quest Type API
 * @param profileProvider the profile provider instance
 */
class CreatePartyEventFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val questTypeApi: QuestTypeApi,
    private val profileProvider: ProfileProvider,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val range = instruction.get(Argument.NUMBER)
        val conditions = instruction.getList(::ConditionID)
        val amount = instruction.getValue("amount", Argument.NUMBER)
        val logger = loggerFactory.create(CreatePartyEvent::class.java)
        val questPackage = instruction.getPackage()
        val createPartyEvent = CreatePartyEvent(questTypeApi, profileProvider, range, conditions, amount, logger)
        return OnlineEventAdapter(createPartyEvent, logger, questPackage)
    }
}
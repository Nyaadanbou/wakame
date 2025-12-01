package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.party

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter

class HasPartyFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val amount = instruction.getValue("amount", Argument.NUMBER_NOT_LESS_THAN_ZERO)
        val logger = loggerFactory.create(HasParty::class.java)
        val hasParty = HasParty(amount, logger)
        val questPackage = instruction.getPackage()
        return OnlineConditionAdapter(hasParty, logger, questPackage)
    }
}
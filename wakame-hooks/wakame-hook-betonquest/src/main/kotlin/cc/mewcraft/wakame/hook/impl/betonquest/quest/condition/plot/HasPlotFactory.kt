package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.plot

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter

class HasPlotFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val amount = instruction.getValue("amount", Argument.NUMBER_NOT_LESS_THAN_ONE)
        val dimension = instruction.getValue("dimension", Argument.STRING)
        val logger = loggerFactory.create(HasPlot::class.java)
        val hasPlot = HasPlot(amount, dimension, logger)
        val questPackage = instruction.getPackage()
        return OnlineConditionAdapter(hasPlot, logger, questPackage)
    }
}
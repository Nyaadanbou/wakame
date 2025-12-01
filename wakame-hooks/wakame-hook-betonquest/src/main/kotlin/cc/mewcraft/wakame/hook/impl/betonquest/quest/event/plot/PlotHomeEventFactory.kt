package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.plot

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter

class PlotHomeEventFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val order = instruction.getValue("order", Argument.NUMBER_NOT_LESS_THAN_ONE)
        val dimension = instruction.getValue("dimension", Argument.WORLD)
        val logger = loggerFactory.create(PlotHomeEvent::class.java)
        val questPackage = instruction.getPackage()
        val plotHomeEvent = PlotHomeEvent(order, dimension, logger)
        return OnlineEventAdapter(plotHomeEvent, logger, questPackage)
    }
}
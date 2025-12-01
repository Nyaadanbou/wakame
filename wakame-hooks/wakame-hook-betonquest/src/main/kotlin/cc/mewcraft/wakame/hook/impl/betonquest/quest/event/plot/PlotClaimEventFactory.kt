package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.plot

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter

class PlotClaimEventFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val skipIfExists = instruction.hasArgument("skipIfExists")
        val dimension = instruction.getValue("dimension", Argument.WORLD)
        val logger = loggerFactory.create(PlotClaimEvent::class.java)
        val questPackage = instruction.getPackage()
        val plotClaimEvent = PlotClaimEvent(skipIfExists, dimension, logger)
        return OnlineEventAdapter(plotClaimEvent, logger, questPackage)
    }
}
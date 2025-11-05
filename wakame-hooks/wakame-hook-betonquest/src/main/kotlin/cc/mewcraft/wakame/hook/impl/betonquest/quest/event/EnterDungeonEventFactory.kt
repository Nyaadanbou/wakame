package cc.mewcraft.wakame.hook.impl.betonquest.quest.event

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter
import org.betonquest.betonquest.api.quest.event.thread.PrimaryServerThreadEvent


class EnterDungeonEventFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val threadData: PrimaryServerThreadData,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val logger = loggerFactory.create(EnterDungeonEvent::class.java)
        val dungeon = instruction.get(Argument.STRING)
        val enterDungeonEvent = OnlineEventAdapter(
            EnterDungeonEvent(dungeon, logger),
            logger,
            instruction.getPackage(),
        )
        return PrimaryServerThreadEvent(enterDungeonEvent, threadData)
    }
}
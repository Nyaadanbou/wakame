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

        // 使用 Instruction#getValue 可以使该事件支持按玩家独立计算的变量
        val useParty = instruction.getValue("party", Argument.BOOLEAN, false)
            ?: error("Failed to get 'party' argument")

        val enterDungeonEvent = OnlineEventAdapter(
            EnterDungeonEvent(dungeon, useParty, logger),
            logger,
            instruction.getPackage(),
        )
        return PrimaryServerThreadEvent(enterDungeonEvent, threadData)
    }
}
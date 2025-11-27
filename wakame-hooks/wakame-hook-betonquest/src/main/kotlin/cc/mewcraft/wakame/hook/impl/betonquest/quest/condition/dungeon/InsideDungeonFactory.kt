package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter
import org.betonquest.betonquest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition

class InsideDungeonFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val data: PrimaryServerThreadData,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val logger = loggerFactory.create(InsideDungeon::class.java)
        val insideDungeon = InsideDungeon(logger)
        val questPackage = instruction.getPackage()
        val synced = OnlineConditionAdapter(insideDungeon, logger, questPackage)
        return PrimaryServerThreadPlayerCondition(synced, data)
    }
}
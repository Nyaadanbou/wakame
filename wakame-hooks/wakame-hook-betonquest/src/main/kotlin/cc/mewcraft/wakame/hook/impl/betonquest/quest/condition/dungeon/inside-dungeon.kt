package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon

import cc.mewcraft.wakame.hook.impl.betonquest.util.MythicDungeonsBridge
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter


class InsideDungeon(
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun isPrimaryThreadEnforced(): Boolean {
        return true
    }

    override fun check(profile: OnlineProfile): Boolean {
        return MythicDungeonsBridge.isInsideDungeon(profile.player)
    }
}


class InsideDungeonFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val logger = loggerFactory.create(InsideDungeon::class.java)
        val insideDungeon = InsideDungeon(logger)
        val questPackage = instruction.getPackage()
        val adapter = OnlineConditionAdapter(insideDungeon, logger, questPackage)
        return adapter
    }
}
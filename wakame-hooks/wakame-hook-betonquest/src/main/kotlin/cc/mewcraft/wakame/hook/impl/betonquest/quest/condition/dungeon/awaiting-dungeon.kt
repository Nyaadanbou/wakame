package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon

import cc.mewcraft.wakame.integration.dungeon.DungeonBridge
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.OnlineConditionAdapter
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory


class AwaitingDungeon(
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun isPrimaryThreadEnforced(): Boolean {
        return true
    }

    override fun check(profile: OnlineProfile): Boolean {
        return DungeonBridge.isAwaitingDungeon(profile.player).getOrThrow()
    }
}


class AwaitingDungeonFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val logger = loggerFactory.create(AwaitingDungeon::class.java)
        val awaitingDungeon = AwaitingDungeon(logger)
        val adapter = OnlineConditionAdapter(awaitingDungeon)
        return adapter
    }
}
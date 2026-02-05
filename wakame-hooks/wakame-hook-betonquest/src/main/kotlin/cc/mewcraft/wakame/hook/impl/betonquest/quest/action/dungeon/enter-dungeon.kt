package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.dungeon

import cc.mewcraft.wakame.integration.dungeon.DungeonBridge
import cc.mewcraft.wakame.integration.party.PartyIntegration
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.OnlineActionAdapter
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory


class EnterDungeonAction(
    private val dungeon: Argument<String>,
    private val logger: BetonQuestLogger,
) : OnlineAction {

    override fun execute(profile: OnlineProfile) {
        val dungeonId = dungeon.getValue(profile)
        if (!DungeonBridge.hasDungeon(dungeonId).getOrThrow()) {
            logger.warn("No dungeon found with name '$dungeonId'")
            return
        }
        // MythicDungeons API 实在是太狗屎了, 这里暂时摸索出一套能用的组队逻辑:
        // 基于 CarbonChat 的 Party 临时创建一个 MythicDungeons 的 Party,
        // 其中, 小队成员和 CarbonChat 中的小队成员一样
        val player = profile.player
        PartyIntegration.lookupPartyByPlayer(player)
            .thenAccept { party ->
                if (party != null) {
                    val leader = player
                    val members = party.players.filter { p -> p != leader && p.location.distance(leader.location) < 16.0 }
                    val players = buildList {
                        add(leader)
                        addAll(members)
                    }
                    DungeonBridge.play(players, dungeonId)
                } else {
                    DungeonBridge.play(player, dungeonId)
                }
            }
    }
}


class EnterDungeonActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val logger = loggerFactory.create(EnterDungeonAction::class.java)
        val dungeon = instruction.string().get()
        val onlineAction = EnterDungeonAction(dungeon, logger)
        val questPackage = instruction.getPackage()
        val adapter = OnlineActionAdapter(onlineAction, logger, questPackage)
        return adapter
    }
}
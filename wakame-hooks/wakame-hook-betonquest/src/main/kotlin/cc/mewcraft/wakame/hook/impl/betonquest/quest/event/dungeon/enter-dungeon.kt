package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.dungeon

import cc.mewcraft.wakame.hook.impl.betonquest.util.MythicDungeonsBridge
import cc.mewcraft.wakame.integration.party.PartyIntegration
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter
import org.betonquest.betonquest.api.quest.event.thread.PrimaryServerThreadEvent
import org.bukkit.Bukkit


class EnterDungeonEvent(
    private val dungeon: Variable<String>,
    private val useParty: Variable<Boolean>,
    private val logger: BetonQuestLogger,
) : OnlineEvent {

    override fun execute(profile: OnlineProfile) {
        val dungeonValue = dungeon.getValue(profile)
        val usePartyValue = useParty.getValue(profile)

        if (!MythicDungeonsBridge.hasDungeon(dungeonValue)) {
            logger.warn("No dungeon found with name '$dungeonValue'")
            return
        }

        // 先使该玩家退出所在小队 (如果有)
        MythicDungeonsBridge.leaveParty(profile.player)

        // MythicDungeons 的 API 实在是太乱了
        //
        // 这里暂时摸索出一套能用的逻辑:
        // 1) 如果 BetonQuest Event 指定了要使用组队功能, 则基于 CarbonChat 的组队信息临时创建一个 MythicDungeons 的小队, 其小队成员和 CarbonChat 中的一样
        // 2) 如果 BetonQuest Event 没有指定要使用组队功能, 则直接把当前玩家传送进去
        if (usePartyValue) {
            sendPartyToDungeon(profile, dungeonValue)
        } else {
            sendSoloToDungeon(profile, dungeonValue)
        }
    }

    private fun sendPartyToDungeon(profile: OnlineProfile, dungeon: String) {
        PartyIntegration.lookupPartyByPlayer(profile.player).thenAccept { koishParty ->
            if (koishParty != null) {
                // 创建一个 MythicDungeons 小队
                val mdParty = MythicDungeonsBridge.createParty(profile.player)
                // 将 KoishParty 的成员添加到 MythicDungeons 小队
                koishParty.members
                    .asSequence()
                    .filter { it != profile.player.uniqueId } // 排除队长
                    .mapNotNull(Bukkit::getPlayer)
                    .filter { it.world == profile.player.world } // 仅包含与队长在同一世界的玩家
                    .forEach { member ->
                        // 先将每个成员从倒计时队列中移除 (如果有)
                        MythicDungeonsBridge.unqueue(member)
                        // 使每个成员退出当前的 MythicDungeons 小队 (如果有)
                        MythicDungeonsBridge.leaveParty(member)
                        // 将该玩家添加到刚才创建的 MythicDungeons 小队
                        mdParty.addPlayer(member)
                    }
                MythicDungeonsBridge.sendToDungeon(profile.player, dungeon)
            } else {
                logger.info("Player ${profile.player.name} is not in a party, but 'party:true' argument was used.")
                sendSoloToDungeon(profile, dungeon)
            }
        }
    }

    private fun sendSoloToDungeon(profile: OnlineProfile, dungeon: String) {
        MythicDungeonsBridge.sendToDungeon(profile.player, dungeon)
    }
}


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

        val onlineEvent = EnterDungeonEvent(dungeon, useParty, logger)
        val questPackage = instruction.getPackage()
        val onlineEventAdapter = OnlineEventAdapter(onlineEvent, logger, questPackage)
        return PrimaryServerThreadEvent(onlineEventAdapter, threadData)
    }
}
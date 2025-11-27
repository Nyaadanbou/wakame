package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.dungeon

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.integration.party.PartyIntegration
import net.playavalon.mythicdungeons.MythicDungeons
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent

class EnterDungeonEvent(
    private val dungeon: Variable<String>,
    private val useParty: Variable<Boolean>,
    private val logger: BetonQuestLogger,
) : OnlineEvent {

    private val mdApi: MythicDungeons
        get() = MythicDungeons.inst()

    override fun execute(profile: OnlineProfile) {
        val player = profile.player
        val dungeonValue = dungeon.getValue(profile)
        val usePartyValue = useParty.getValue(profile)

        if (mdApi.dungeonManager.get(dungeonValue) == null) {
            logger.warn("No dungeon found with name '$dungeonValue'")
            return
        }

        // 先使该玩家退出所在小队 (如果有)
        mdApi.removeFromParty(player)

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
        val player = profile.player
        PartyIntegration.lookupPartyByPlayer(player).thenAccept { koishParty ->
            if (koishParty != null) {
                // 创建一个 MythicDungeons 小队
                mdApi.createParty(player)
                // 获取刚才创建的 MythicDungeons 小队
                val mdParty = mdApi.getParty(player) ?: run {
                    logger.error("Failed to get MythicDungeons party for player ${profile.player.name}.")
                    return@thenAccept
                }
                // 将 KoishParty 的成员添加到 MythicDungeons 小队
                koishParty.members
                    .asSequence()
                    .filter { it != player.uniqueId } // 排除队长
                    .mapNotNull(SERVER::getPlayer)
                    .filter { it.world == player.world } // 仅包含与队长在同一世界的玩家
                    .forEach { member ->
                        // 使每个成员退出当前的 MythicDungeons 小队 (如果有)
                        mdApi.removeFromParty(member)
                        // 将该玩家添加到刚才创建的 MythicDungeons 小队
                        mdParty.addPlayer(member)
                    }
                mdApi.sendToDungeon(player, dungeon)
            } else {
                logger.info("Player ${profile.player.name} is not in a party, but 'party:true' argument was used.")
                sendSoloToDungeon(profile, dungeon)
            }
        }
    }

    private fun sendSoloToDungeon(profile: OnlineProfile, dungeon: String) {
        val player = profile.player
        mdApi.sendToDungeon(player, dungeon)
    }
}
package cc.mewcraft.wakame.hook.impl.betonquest.quest.event

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
        val resolvedDungeon = dungeon.getValue(profile)
        val resolvedUseParty = useParty.getValue(profile)

        if (mdApi.dungeonManager.get(resolvedDungeon) == null) {
            logger.warn("No dungeon found with name '$resolvedDungeon'")
            return
        }

        // 先让该玩家退出所在队伍 (如果有)
        mdApi.removeFromParty(player)

        // MythicDungeons 的 API 实在是太乱了
        //
        // 这里暂时摸索出一套能用的逻辑:
        // 1) 如果 BetonQuest Event 指定了要使用组队功能, 则基于 CarbonChat 的组队信息临时创建一个 MythicDungeons 的队伍, 其队伍成员和 CarbonChat 中的一样
        // 2) 如果 BetonQuest Event 没有指定要使用组队功能, 则直接把当前玩家传送进去
        if (resolvedUseParty) {
            PartyIntegration.lookupPartyByPlayer(profile.player)
                .thenAccept { party ->
                    if (party != null) {
                        // 找到了 Koish 队伍

                        // 创建一个 MythicDungeons 队伍
                        mdApi.createParty(player)

                        // 获取刚才创建的 MythicDungeons 队伍
                        val mdParty = mdApi.getParty(player) ?: run {
                            logger.error("Failed to get MythicDungeons party for player ${profile.player.name}.")
                            return@thenAccept
                        }

                        party.members
                            .filter { it != player.uniqueId } // 排除队长
                            .mapNotNull { SERVER.getPlayer(it) }
                            .filter { it.location.distance(player.location) < 8 }
                            .forEach {
                                // 销毁其他队伍成员所在的 MythicDungeons 队伍
                                mdApi.removeFromParty(player)

                                // 将该玩家添加到刚才创建的 MythicDungeons 队伍
                                mdParty.addPlayer(it)
                            }

                        mdApi.sendToDungeon(player, resolvedDungeon)
                    } else {
                        logger.info("Player ${profile.player.name} is not in a party, but 'party:true' argument was used.")
                    }
                }
        } else {
            mdApi.removeFromParty(player)
            mdApi.sendToDungeon(player, resolvedDungeon)
        }
    }
}
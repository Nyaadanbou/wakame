package cc.mewcraft.wakame.hook.impl.betonquest

import net.playavalon.mythicdungeons.MythicDungeons
import net.playavalon.mythicdungeons.player.party.partysystem.MythicParty
import org.bukkit.entity.Player

object MythicDungeonsApi {

    private val mdApi: MythicDungeons
        get() = MythicDungeons.inst()

    /**
     * 将玩家从倒计时队列中移除.
     */
    fun unqueue(player: Player) {
        val mdPlayer = mdApi.getMythicPlayer(player) ?: return
        mdApi.queueManager.unqueue(mdPlayer)
    }

    /**
     * 将玩家送到指定地牢. 如果他有小队, 则整个小队都会进去.
     */
    fun sendToDungeon(player: Player, dungeonId: String) {
        mdApi.sendToDungeon(player, dungeonId)
    }

    /**
     * 玩家是否正在等待进入地牢 (读倒计时).
     */
    fun isAwaitingDungeon(player: Player): Boolean {
        val mdPlayer = mdApi.getMythicPlayer(player) ?: return false
        return mdPlayer.isAwaitingDungeon
    }

    /**
     * 检查玩家是否在地牢内.
     */
    fun isInsideDungeon(player: Player): Boolean {
        return mdApi.isPlayerInDungeon(player)
    }

    /**
     * 检查指定地牢是否存在.
     */
    fun hasDungeon(id: String): Boolean {
        return mdApi.dungeonManager.get(id) != null
    }

    /**
     * 使玩家离开当前所在小队 (如果有的话).
     */
    fun leaveParty(player: Player) {
        mdApi.removeFromParty(player)
    }

    /**
     * 为玩家创建一个新的小队, 并返回该小队对象.
     */
    fun createParty(player: Player): MythicParty {
        leaveParty(player)
        mdApi.createParty(player)
        val mdParty = mdApi.getParty(player) ?: throw IllegalStateException("Failed to get MythicDungeons party for player ${player.name}")
        return mdParty
    }
}
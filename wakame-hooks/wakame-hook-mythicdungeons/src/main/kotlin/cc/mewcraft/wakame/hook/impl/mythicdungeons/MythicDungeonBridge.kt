package cc.mewcraft.wakame.hook.impl.mythicdungeons

import cc.mewcraft.wakame.integration.dungeon.DungeonBridge
import cc.mewcraft.wakame.integration.party.Party
import net.playavalon.mythicdungeons.MythicDungeons
import net.playavalon.mythicdungeons.player.party.partysystem.MythicParty
import org.bukkit.entity.Player

class MythicDungeonBridge : DungeonBridge {

    private val mdApi: MythicDungeons
        get() = MythicDungeons.inst()

    override fun inited(): Boolean {
        return true
    }

    override fun hasDungeon(id: String): Result<Boolean> {
        return runCatching { mdApi.dungeonManager.get(id) != null }
    }

    override fun play(player: Player, dungeon: String): Result<Unit> {
        return runCatching {
            ensureReady(player)
            mdApi.sendToDungeon(player, dungeon)
        }
    }

    override fun play(players: List<Player>, dungeon: String): Result<Unit> {
        return runCatching {
            val mythicParty = createParty0(players)
            val leader = mythicParty.leader
            mdApi.sendToDungeon(leader, dungeon)
        }
    }

    override fun unqueue(player: Player): Result<Unit> {
        return runCatching {
            val mythicPlayer = mdApi.getMythicPlayer(player) ?: return@runCatching
            mdApi.queueManager.unqueue(mythicPlayer)
        }
    }

    override fun isAwaitingDungeon(player: Player): Result<Boolean> {
        return runCatching { mdApi.getMythicPlayer(player)?.isAwaitingDungeon ?: false }
    }

    override fun isInsideDungeon(player: Player): Result<Boolean> {
        return runCatching { mdApi.isPlayerInDungeon(player) }
    }

    override fun leaveParty(player: Player): Result<Unit> {
        return runCatching { ensureReady(player) }
    }

    override fun createParty(player: Player): Result<Party> {
        return runCatching { MythicPartyWrapper(createParty0(player)) }
    }

    override fun createParty(players: List<Player>): Result<Party> {
        return runCatching { MythicPartyWrapper(createParty0(players)) }
    }

    private fun ensureReady(player: Player) {
        mdApi.removeFromParty(player)
        val mythicPlayer = mdApi.getMythicPlayer(player) ?: return
        mdApi.queueManager.unqueue(mythicPlayer)
    }

    private fun createParty0(player: Player): MythicParty {
        ensureReady(player)
        mdApi.createParty(player)
        return mdApi.getParty(player) ?: throw IllegalStateException("Failed to create MythicParty for player ${player.name}")
    }

    private fun createParty0(players: List<Player>): MythicParty {
        val leader = players[0]
        val members = players.drop(1)
        ensureReady(leader)
        members.forEach(::ensureReady)
        mdApi.createParty(leader)
        val mythicParty = mdApi.getParty(leader) ?: throw IllegalStateException("Failed to create MythicParty for player ${leader.name}")
        members.forEach(mythicParty::addPlayer)
        return mythicParty
    }
}
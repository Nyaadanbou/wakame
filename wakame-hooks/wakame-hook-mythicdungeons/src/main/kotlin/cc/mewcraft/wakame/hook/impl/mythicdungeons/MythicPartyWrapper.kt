package cc.mewcraft.wakame.hook.impl.mythicdungeons

import cc.mewcraft.wakame.integration.party.Party
import net.kyori.adventure.text.Component
import net.playavalon.mythicdungeons.player.party.partysystem.MythicParty
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class MythicPartyWrapper(
    private val handle: MythicParty,
) : Party {

    override val name: Component
        get() = Component.text("${handle.leader.name}'s MythicParty")
    override val id: UUID
        get() = handle.leader.uniqueId
    override val members: Set<UUID>
        get() = handle.players.map(Player::getUniqueId).toSet()
    override val players: Set<Player>
        get() = handle.players.toSet()

    override fun addMember(id: UUID) {
        val player = Bukkit.getPlayer(id) ?: return
        handle.addPlayer(player)
    }

    override fun removeMember(id: UUID) {
        val player = Bukkit.getPlayer(id) ?: return
        handle.removePlayer(player)
    }

    override fun disband() {
        handle.disband()
    }
}
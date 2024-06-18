package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.Player as BukkitPlayer

sealed interface Caster : Audience {
    interface Player : Caster {
        val bukkitPlayer: BukkitPlayer
    }

    interface Entity : Caster {
        val bukkitEntity: BukkitEntity
    }
}

object CasterAdapter {
    fun adapt(user: User<Player>): Caster.Player {
        return PlayerCaster(user.player)
    }

    fun adapt(player: Player): Caster.Player {
        return PlayerCaster(player)
    }
}

private data class PlayerCaster(
    override val bukkitPlayer: BukkitPlayer
) : Caster.Player {
    @Suppress("OVERRIDE_DEPRECATION", "UnstableApiUsage")
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        bukkitPlayer.sendMessage(source, message, type)
    }

    override fun deleteMessage(signature: SignedMessage.Signature) {
        bukkitPlayer.deleteMessage(signature)
    }
}
package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.Player as BukkitPlayer

sealed interface Caster {
    interface Entity : Caster {
        val bukkitEntity: BukkitEntity
    }

    interface Player : Entity {
        override val bukkitEntity: BukkitEntity
            get() = bukkitPlayer
        val bukkitPlayer: BukkitPlayer
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
) : Caster.Player
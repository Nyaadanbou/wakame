package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player
import org.bukkit.entity.Player as BukkitPlayer

sealed interface Caster {
    interface Void : Caster {

    }

    interface Player : Caster {
        val bukkitPlayer: BukkitPlayer
    }
}

object CasterAdapter {
    fun adapt(user: User<Player>): Caster.Player {
        return object : Caster.Player {
            override val bukkitPlayer: Player = user.player
        }
    }
    fun adapt(player: Player): Caster.Player {
        return object : Caster.Player {
            override val bukkitPlayer: Player = player
        }
    }

    fun adapt(): Caster.Void {
        return object : Caster.Void {
        }
    }
}
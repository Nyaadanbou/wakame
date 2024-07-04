package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.WakameInjections
import com.github.retrooper.packetevents.protocol.player.User
import org.bukkit.Server
import org.bukkit.entity.Player
import org.koin.core.component.get

val User.bukkitPlayer: Player
    get() = checkNotNull(WakameInjections.get<Server>().getPlayer(this.uuid)) { "Player not found" }
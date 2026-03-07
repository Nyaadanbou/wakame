package cc.mewcraft.wakame.entity.player

import org.bukkit.entity.Player

interface OnlineUserTicker {
    fun onTickUser(user: User, player: Player) = Unit
}
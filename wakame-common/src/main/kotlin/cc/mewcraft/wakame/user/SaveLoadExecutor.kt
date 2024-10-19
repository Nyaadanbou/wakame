package cc.mewcraft.wakame.user

import org.bukkit.entity.Player

interface SaveLoadExecutor {
    fun saveTo(player: Player)
    fun loadFrom(player: Player)
}
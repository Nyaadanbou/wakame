package cc.mewcraft.wakame.mixin.support

import org.bukkit.entity.Player

object ContainerSyncSession {

    private val threadLocalPlayer = ThreadLocal.withInitial<Player> { null }

    fun setPlayer(player: Player) {
        threadLocalPlayer.set(player)
    }

    fun unsetPlayer() {
        threadLocalPlayer.remove()
    }

    fun getPlayer(): Player? {
        return threadLocalPlayer.get()
    }
}
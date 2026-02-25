package cc.mewcraft.wakame.mixin.support

import io.papermc.paper.util.SafeAutoClosable
import org.bukkit.entity.Player

object ContainerSyncSession : SafeAutoClosable {

    private val THREAD_LOCAL_PLAYER = ThreadLocal.withInitial<Player> { null }

    fun start(player: Player): ContainerSyncSession {
        THREAD_LOCAL_PLAYER.set(player)
        return this
    }

    fun player(): Player? {
        return THREAD_LOCAL_PLAYER.get()
    }

    override fun close() {
        THREAD_LOCAL_PLAYER.remove()
    }
}
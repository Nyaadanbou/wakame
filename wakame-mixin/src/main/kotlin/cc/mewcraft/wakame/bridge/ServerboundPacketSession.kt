package cc.mewcraft.wakame.bridge

import io.papermc.paper.util.SafeAutoClosable
import org.bukkit.entity.Player

/**
 * 用于优雅的将 [Player] 对象传递到 Koish 的网络物品逻辑中.
 */
object ServerboundPacketSession : SafeAutoClosable {

    private val THREAD_LOCAL_PLAYER = ThreadLocal.withInitial<Player> { null }

    fun start(player: Player): ServerboundPacketSession {
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
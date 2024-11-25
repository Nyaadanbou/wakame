package cc.mewcraft.wakame.util.concurrent

import org.bukkit.Bukkit
import org.spigotmc.WatchdogThread

internal val isServerThread: Boolean
    get() = Bukkit.isPrimaryThread() || Thread.currentThread() is WatchdogThread

internal fun checkServerThread() {
    check(isServerThread) { "not on server thread" }
}
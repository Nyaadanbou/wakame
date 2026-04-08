package cc.mewcraft.wakame.util.concurrent

import org.bukkit.Bukkit
import org.spigotmc.WatchdogThread

val isServerThread: Boolean
    get() = Bukkit.isPrimaryThread() || Thread.currentThread() is WatchdogThread

fun checkServerThread() {
    check(isServerThread) { "Not on server thread" }
}
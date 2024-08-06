package cc.mewcraft.wakame.util.coroutine

import cc.mewcraft.wakame.PluginProvider
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.Dispatchers
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

private val plugin: Plugin by PluginProvider.lazy()

val Dispatchers.BukkitMain: CoroutineContext
    get() = plugin.minecraftDispatcher

val Dispatchers.BukkitAsync: CoroutineContext
    get() = plugin.asyncDispatcher
package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.scheme.WakaItem
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

object NekoItemRegistry : KoinComponent, Initializable, Reloadable,
    Registry<Key, WakaItem> by RegistryBase() {

    // constants

    // configuration stuff

    fun get(key: String): WakaItem? = get(Key.key(key))
    fun getOrThrow(key: String): WakaItem = getOrThrow(Key.key(key))

    private fun loadConfiguration() {
        // TODO finish it up
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}
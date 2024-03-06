package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.reloadable
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableBiMap
import com.google.common.collect.Maps
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader


@ReloadDependency(
    runAfter = [ResourcePackManager::class]
)
internal class CustomModelDataConfiguration(
    private val loader: GsonConfigurationLoader,
) : Initializable {
    private val root: BasicConfigurationNode by reloadable { loader.load() }

    private val _customModelDataMap: BiMap<Key, Int> = Maps.synchronizedBiMap(HashBiMap.create())

    val customModelDataMap: ImmutableBiMap<Key, Int>
        get() = ImmutableBiMap.copyOf(_customModelDataMap)

    private fun loadLayout() {
        _customModelDataMap.clear()

        for ((k, v) in root.childrenMap()) {
            val key = Key.key(k.toString())
            val value = v.int
            _customModelDataMap[key] = value
        }
    }

    fun saveCustomModelData(key: Key): Int {
        val newValue = _customModelDataMap.computeIfAbsent(key) {
            _customModelDataMap.maxByOrNull { it.value }?.value?.let { it + 1 } ?: 10000
        }
        saveCustomModelData(root)
        return newValue
    }

    fun removeCustomModelData(vararg keys: Key): Boolean {
        if (keys.isEmpty()) return false
        keys.forEach { _customModelDataMap.remove(it) }
        saveCustomModelData(root)
        return true
    }

    fun removeCustomModelData(vararg customModelData: Int): Boolean {
        if (customModelData.isEmpty()) return false
        customModelData.forEach { _customModelDataMap.inverse().remove(it) }
        saveCustomModelData(root)
        return true
    }

    private fun saveCustomModelData(node: BasicConfigurationNode) {
        node.raw(null)

        _customModelDataMap.forEach { (key, value) ->
            if (node.node(key.asString()).virtual()) {
                node.node(key.asString()).set(value)
            }
        }

        loader.save(node)
    }

    override fun onPrePack() {
        loadLayout()
    }

    override fun onReload() {
        loadLayout()
    }
}
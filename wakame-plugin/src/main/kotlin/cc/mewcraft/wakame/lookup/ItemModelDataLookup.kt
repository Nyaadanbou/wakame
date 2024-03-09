package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.reloadable
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.google.common.collect.Tables
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader


@ReloadDependency(
    runAfter = [ResourcePackManager::class]
)
internal class ItemModelDataLookup(
    private val loader: GsonConfigurationLoader,
) : Initializable {
    private val root: BasicConfigurationNode by reloadable { loader.load() }

    private val customModelDataMap: Table<Key, Int, Int> = Tables.synchronizedTable(HashBasedTable.create())

    private fun loadLayout() {
        customModelDataMap.clear()

        root.childrenMap().forEach { (key, node) ->
            val k = Key.key(key.toString())
            node.childrenMap().forEach { (sid, cmd) ->
                customModelDataMap.put(k, sid as Int, cmd.int)
            }
        }
    }

    operator fun get(key: Key, sid: Int): Int {
        return customModelDataMap.get(key, sid) ?: throw NullPointerException(key.asString())
    }

    fun saveCustomModelData(key: Key, sid: Int): Int {
        val maxVal = customModelDataMap.values().maxOrNull() ?: 0
        val newVal = maxVal + 1
        return customModelDataMap.put(key, sid, newVal).also { saveCustomModelData(root) } ?: newVal
    }

    fun removeCustomModelData(key: Key, sid: Int): Int? {
        return customModelDataMap.remove(key, sid).also { saveCustomModelData(root) }
    }

    fun removeCustomModelData(vararg values: Int): Boolean {
        return values.map { customModelDataMap.values().remove(it) }.any { it }.also { saveCustomModelData(root) }
    }

    /**
     * 根据目前的 [customModelDataMap] 保存到配置文件
     */
    private fun saveCustomModelData(node: BasicConfigurationNode) {
        node.set(null)

        customModelDataMap.rowMap().forEach { (key, map) ->
            map.forEach { (sid, cmd) ->
                node.node(key.asString(), sid).set(cmd)
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
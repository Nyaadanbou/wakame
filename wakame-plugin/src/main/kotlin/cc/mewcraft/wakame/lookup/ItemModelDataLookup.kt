package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.requireKt
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

    private val customModelDataTable: Table<Key, Int, Int> = Tables.synchronizedTable(HashBasedTable.create())

    private fun loadLayout() {
        customModelDataTable.clear()
        val map: Map<String, Int> = root.requireKt()

        map.forEach { (key, value) ->
            val (row, column) = key.split("-")
            customModelDataTable.put(Key.key(row), column.toInt(), value)
        }
    }

    operator fun get(key: Key, variant: Int): Int {
        return customModelDataTable.get(key, variant) ?: throw NullPointerException(key.asString())
    }

    fun saveCustomModelData(key: Key, variant: Int): Int {
        val oldValue = customModelDataTable.get(key, variant)
        if (oldValue != null) return oldValue

        // 如果不存在则创建一个新的
        val maxVal = customModelDataTable.values().maxOrNull()
        val newVal = maxVal?.plus(1) ?: 10000
        return customModelDataTable.put(key, variant, newVal).also { saveCustomModelData(root) } ?: newVal
    }

    fun removeCustomModelData(key: Key, variant: Int): Int? {
        return customModelDataTable.remove(key, variant).also { saveCustomModelData(root) }
    }

    fun removeCustomModelData(vararg values: Int): Boolean {
        if (values.isEmpty()) return false
        return values.map { customModelDataTable.values().remove(it) }.any { it }.also { saveCustomModelData(root) }
    }

    /**
     * 根据目前的 [customModelDataTable] 保存到配置文件
     */
    private fun saveCustomModelData(node: BasicConfigurationNode) {
        node.set(null)

        customModelDataTable.cellSet().forEach {
            val k = it.rowKey.asString()
            val s = it.columnKey
            val v = it.value
            node.node("$k-$s").set(v)
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
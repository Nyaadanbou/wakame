package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.google.common.collect.Tables
import net.kyori.adventure.key.Key
import org.slf4j.Logger
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader


internal class ItemModelDataLookup(
    private val logger: Logger,
    private val loader: GsonConfigurationLoader,
) : Initializable {
    private val root: BasicConfigurationNode by ReloadableProperty { loader.load() }

    private val customModelDataTable: Table<Key, Int, Int> = Tables.synchronizedTable(HashBasedTable.create())

    private fun loadLayout() {
        customModelDataTable.clear()
        for (entry in root.childrenMap()) {
            val key = entry.key.toString()
            val valueNode = entry.value
            for (variant in valueNode.childrenMap()) {
                val variantKey = variant.key.toString().toInt()
                val variantValue = variant.value.krequire<Int>()
                customModelDataTable.put(Key(key), variantKey, variantValue)
            }
        }
        customModelDataTable.rowKeySet().forEach { logger.info("<gold>Loaded custom model data for $it") }
    }

    operator fun get(key: Key, variant: Int): Int? {
        return customModelDataTable.get(key, variant)
    }

    fun saveCustomModelData(key: Key, variant: Int): Int {
        val oldValue = customModelDataTable.get(key, variant)
        if (oldValue != null) return oldValue

        val maxVal = customModelDataTable.values().maxOrNull()
        // 如果存在最大值则取其并加一，否则从 10000 开始
        val newVal = maxVal?.plus(1) ?: 10000
        return customModelDataTable.put(key, variant, newVal).also { saveCustomModelData(root) } ?: newVal
    }

    fun removeCustomModelData(key: Key, variant: Int): Int? {
        return customModelDataTable.remove(key, variant).also { saveCustomModelData(root) }
    }

    fun removeCustomModelData(vararg values: Int): Boolean {
        if (values.isEmpty()) return false
        return values.any { customModelDataTable.values().remove(it) }.also { saveCustomModelData(root) }
    }

    /**
     * 根据目前的 [customModelDataTable] 保存到配置文件
     */
    private fun saveCustomModelData(node: BasicConfigurationNode) {
        node.set(null)

        for (cell in customModelDataTable.cellSet()) {
            val k = cell.rowKey.asString()
            val s = cell.columnKey
            val v = cell.value
            node.node(k, s.toString()).set(v)
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
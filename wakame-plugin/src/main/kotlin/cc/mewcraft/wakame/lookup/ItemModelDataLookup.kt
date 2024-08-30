package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import net.kyori.adventure.key.Key
import org.slf4j.Logger
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader

@PreWorldDependency(runBefore = [ItemRegistry::class])
internal class ItemModelDataLookup(
    private val logger: Logger,
    private val loader: GsonConfigurationLoader,
) : Initializable {
    private val root: BasicConfigurationNode by ReloadableProperty { loader.load() }

    /**
     * 用于存储自定义模型数据的表.
     *
     * R - 物品的 key.
     * C - 物品的变体.
     * V - CustomModelData 的值.
     */
    private val customModelDataTable: Table<Key, Int, Int> = HashBasedTable.create()

    private fun loadCustomModelDataTable() {
        customModelDataTable.clear()

        for ((nodeKey, valueNode) in root.childrenMap()) {
            for ((nodeKey1, valueNode1) in valueNode.childrenMap()) {
                val itemId = Key(nodeKey.toString())
                val variant = nodeKey1.toString().toIntOrNull() ?: throw IllegalArgumentException("Invalid variant: '$nodeKey1'")
                val customModelData = valueNode1.krequire<Int>()
                customModelDataTable.put(itemId, variant, customModelData)
            }
        }

        logger.info("Loaded custom model data for items: {}", customModelDataTable.rowKeySet().map(Key::asString).joinToString())
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

    override fun onPreWorld() {
        loadCustomModelDataTable()
    }

    override fun onReload() {
        loadCustomModelDataTable()
    }
}
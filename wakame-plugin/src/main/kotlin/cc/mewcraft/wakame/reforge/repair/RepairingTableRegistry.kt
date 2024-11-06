package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key

/**
 * 修复台的注册表.
 */
object RepairingTableRegistry : Initializable {
    private val items: MutableMap<Key, PriceInstance> = mutableMapOf()
    private val tables: MutableMap<String, RepairingTable> = mutableMapOf()

    fun getItem(id: Key): PriceInstance? {
        return items[id]
    }

    fun getTable(id: String): RepairingTable? {
        return tables[id]
    }

    private fun load() {
        // 物品价格实例必须先加载,
        // 然后再加载修复台实例.
        //
        // 因为创建修复台依赖

        items.clear()
        items.putAll(RepairingTableSerializer.loadAllItems())

        tables.clear()
        tables.putAll(RepairingTableSerializer.loadAllTables())
        tables.put("wtf", WtfRepairingTable) // 总是覆盖同名映射
    }

    override fun onPostWorld() {
        load()
    }

    override fun onReload() {
        load()
    }
}
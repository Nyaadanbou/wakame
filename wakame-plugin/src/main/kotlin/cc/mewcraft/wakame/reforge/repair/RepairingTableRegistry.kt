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

    val names: Set<String>
        get() = tables.keys

    fun getItem(id: Key): PriceInstance? {
        return items[id]
    }

    fun getTable(id: String): RepairingTable? {
        return tables[id]
    }

    private fun load() {
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
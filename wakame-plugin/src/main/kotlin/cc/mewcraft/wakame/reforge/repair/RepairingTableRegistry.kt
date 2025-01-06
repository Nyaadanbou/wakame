package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting

/**
 * 修复台的注册表.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload()
object RepairingTableRegistry {
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

    @VisibleForTesting
    fun load() {
        items.clear()
        items.putAll(RepairingTableSerializer.loadAllItems())

        tables.clear()
        tables.putAll(RepairingTableSerializer.loadAllTables())
        tables.put("wtf", WtfRepairingTable) // 总是覆盖同名映射
    }

    @InitFun
    private fun onPostWorld() {
        load()
    }

    @ReloadFun
    private fun onReload() {
        load()
    }
}
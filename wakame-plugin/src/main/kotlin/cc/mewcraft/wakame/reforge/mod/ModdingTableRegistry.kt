package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun

/**
 * 定制台的注册表.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload()
object ModdingTableRegistry {
    private val tables = HashMap<String, ModdingTable>()

    /**
     * 获取所有定制台的唯一标识.
     */
    val NAMES: Set<String>
        get() = tables.keys

    @InitFun
    fun init() {
        load()
    }

    @ReloadFun
    fun reload() {
        load()
    }

    /**
     * 获取指定的定制台.
     */
    operator fun get(id: String): ModdingTable? {
        return tables[id]
    }

    private fun load() {
        tables.clear()

        val tables = ModdingTableSerializer.loadAll()
        this.tables.putAll(tables)
        this.tables.put("wtf", WtfModdingTable)
    }
}
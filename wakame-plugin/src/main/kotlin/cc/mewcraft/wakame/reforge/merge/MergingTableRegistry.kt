package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

/**
 * 合并台的注册表.
 */
@Init(InitStage.POST_WORLD)
object MergingTableRegistry {
    private val tables = HashMap<String, MergingTable>()

    /**
     * 获取所有定制台的唯一标识.
     */
    val NAMES: Set<String>
        get() = tables.keys

    @InitFun
    fun init() {
        load()
    }

    fun reload() {
        load()
    }

    /**
     * 获取指定的定制台.
     */
    operator fun get(id: String): MergingTable? {
        return tables[id]
    }

    private fun load() {
        tables.clear()
        val tables = MergingTableSerializer.loadAll()
        this.tables.putAll(tables)
        this.tables.put("wtf", WtfMergingTable)
    }
}
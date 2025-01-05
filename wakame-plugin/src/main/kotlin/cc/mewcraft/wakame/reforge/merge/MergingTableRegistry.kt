package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadableFun
import cc.mewcraft.wakame.reloader.ReloadableOrder

/**
 * 合并台的注册表.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload(
    order = ReloadableOrder.NORMAL
)
object MergingTableRegistry {
    private val tables = HashMap<String, MergingTable>()

    /**
     * 获取所有定制台的唯一标识.
     */
    val NAMES: Set<String>
        get() = tables.keys

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

    @InitFun
    fun onPostWorld() {
        load()
    }

    @ReloadableFun
    fun onReload() {
        load()
    }
}
package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage

/**
 * 重造台的注册表.
 */
@Init(
    stage = InitStage.POST_WORLD
)
object RerollingTableRegistry  {
    private val tables = HashMap<String, RerollingTable>()

    /**
     * 获取所有定制台的唯一标识.
     */
    val NAMES: Set<String>
        get() = tables.keys

    /**
     * 获取指定的定制台.
     */
    operator fun get(id: String): RerollingTable? {
        return tables[id]
    }

    private fun load() {
        tables.clear()
        val tables = RerollingTableSerializer.loadAll()
        this.tables.putAll(tables)
        this.tables.put("wtf", WtfRerollingTable)
    }

    @InitFun
    fun onPostWorld() {
        load()
    }

//    override fun onReload() {
//        load()
//    }
}
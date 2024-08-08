package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.initializer.Initializable

/**
 * 重造台的注册表.
 */
object RerollingTables : Initializable {
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

    override fun onPostWorld() {
        load()
    }

    override fun onReload() {
        load()
    }
}
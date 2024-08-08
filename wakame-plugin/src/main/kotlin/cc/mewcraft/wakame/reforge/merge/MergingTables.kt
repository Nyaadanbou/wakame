package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.initializer.Initializable

/**
 * 合并台的注册表.
 */
object MergingTables : Initializable {
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

    override fun onPostWorld() {
        load()
    }

    override fun onReload() {
        load()
    }
}
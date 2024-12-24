package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.AbilityRegistry

/**
 * 定制台的注册表.
 */
// 在 PreWorld 早已加载的依赖不需要指定 PostWorld
@ReloadDependency(
    runBefore = [
        // 我们仍然直接依赖 Ability 相关的实例, 而不是实例的引用, 因此 Ability 必须在我们之前加载完毕
        AbilityRegistry::class
    ]
)
object ModdingTableRegistry : Initializable {
    private val tables = HashMap<String, ModdingTable>()

    /**
     * 获取所有定制台的唯一标识.
     */
    val NAMES: Set<String>
        get() = tables.keys

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

    override fun onPostWorld() {
        load()
    }

    override fun onReload() {
        load()
    }
}
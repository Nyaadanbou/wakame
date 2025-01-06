package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun

/**
 * 定制台的注册表.
 */
// 在 PreWorld 早已加载的依赖不需要指定 PostWorld
//@ReloadDependency(
//    runBefore = [
//        // 我们仍然直接依赖 Ability 相关的实例, 而不是实例的引用, 因此 Ability 必须在我们之前加载完毕
//        AbilityRegistry::class
//    ]
//)
@Init(
    stage = InitStage.POST_WORLD
)
@Reload(
    runAfter = [
        // 我们仍然直接依赖 Ability 相关的实例, 而不是实例的引用, 因此 Ability 必须在我们之前加载完毕
        AbilityRegistry::class
    ]
)
object ModdingTableRegistry {
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

    @InitFun
    private fun onPostWorld() {
        load()
    }

    @ReloadFun
    private fun onReload() {
        load()
    }
}
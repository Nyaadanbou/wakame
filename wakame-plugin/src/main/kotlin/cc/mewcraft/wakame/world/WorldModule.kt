// 本 package 负责所有游戏世界之内的逻辑实现
// TODO 逐步把现有的游戏世界内的逻辑迁移到这里

package cc.mewcraft.wakame.world

import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.entity.EntityKeyLookupImpl
import cc.mewcraft.wakame.entity.UnbreakableArmorStand
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import cc.mewcraft.wakame.world.player.death.PlayerDeathProtect
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun worldModule(): Module = module {
    //<editor-fold desc="Root">
    //</editor-fold>

    //<editor-fold desc="Entity">
    singleOf(::UnbreakableArmorStand)

    single<EntityKeyLookup> {
        fun registerImplementation(
            requiredPlugin: String,
            implementations: MutableList<EntityKeyLookup.Dictionary>,
            implementationCreator: () -> EntityKeyLookup.Dictionary,
        ) {
            if (NEKO.isPluginPresent(requiredPlugin)) {
                implementations.add(implementationCreator())
            }
        }

        EntityKeyLookupImpl(buildList {
            // 目前的所有实现暂时不需要获取 MythicMobs 的怪物的 id, 等之后需要的时候再把这个注释给去掉.
            // registerImplementation("MythicMobs", this, ::MythicMobsEntityKeyLookup)
        })
    }
    //</editor-fold>

    //<editor-fold desc="Fake Entity">
    //</editor-fold>

    //<editor-fold desc="Item">
    //</editor-fold>

    //<editor-fold desc="Model">
    //</editor-fold>

    //<editor-fold desc="Player">
    singleOf(::PlayerDeathProtect)
    //</editor-fold>
}
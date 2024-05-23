package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.binary.cell.core.attribute.AttributeCoreInitializer
import cc.mewcraft.wakame.item.binary.cell.core.empty.EmptyCoreInitializer
import cc.mewcraft.wakame.item.binary.cell.core.skill.SkillCoreInitializer
import cc.mewcraft.wakame.item.binary.meta.ItemMetaInitializer
import cc.mewcraft.wakame.item.binary.meta.ItemMetaLineKeyFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun binaryItemModule(): Module = module {
    singleOf(::ItemMetaLineKeyFactory)
    single { AttributeCoreInitializer } withOptions { bind<Initializable>() }
    single { EmptyCoreInitializer } withOptions { bind<Initializable>() }
    single { SkillCoreInitializer } withOptions { bind<Initializable>() }
    single { ItemMetaInitializer } withOptions { bind<Initializable>() }
}
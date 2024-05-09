package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.binary.meta.ItemMetaLineKeyFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun binaryItemModule(): Module = module {
    singleOf(::ItemMetaLineKeyFactory)
}
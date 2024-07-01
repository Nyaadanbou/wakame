package cc.mewcraft.wakame.item.templates.filter

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val ITEM_TEMPLATE_FILTER_SERIALIZER = "item_template_filter_serializer"

internal fun filterModule(): Module = module {
    single(named(ITEM_TEMPLATE_FILTER_SERIALIZER)) { FilterSerializer }
}
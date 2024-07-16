package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.item.templates.filter.ItemFilterNodeReader
import cc.mewcraft.wakame.random3.FilterNodeFacade
import cc.mewcraft.wakame.random3.FilterNodeReader
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ITEM_FILTER_NODE_READER = "item_filter_node_reader"
internal const val ITEM_FILTER_NODE_FACADE = "item_filter_node_facade"
internal const val ITEM_COMPONENT_TEMPLATE_SERIALIZERS = "item_component_template_serializers"

internal fun templateModule(): Module = module {
    // FilterNode ...
    single<FilterNodeReader<GenerationContext>>(named(ITEM_FILTER_NODE_READER)) {
        ItemFilterNodeReader()
    }
    single<FilterNodeFacade<GenerationContext>>(named(ITEM_FILTER_NODE_FACADE)) {
        // 开发日记 2024/7/16
        // 整个 item 模块用一个 FilterNodeFacade 实例就行
        FilterNodeFacade(
            Configs.YAML["random/items/filters.yml"],
            get(named(ITEM_FILTER_NODE_READER))
        )
    }

    // Components ...
    single<TypeSerializerCollection>(named(ITEM_COMPONENT_TEMPLATE_SERIALIZERS)) {
        ItemTemplateTypes.collectTypeSerializers()
    }
}
package cc.mewcraft.wakame.item.template

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ITEM_COMPONENT_TEMPLATE_SERIALIZERS = "item_component_template_serializers"

internal fun templateModule(): Module = module {
    // FilterNode ...
//    single<ItemFilterNodeFacade> {
//        // 开发日记 2024/7/16
//        // 整个 item 模块用一个 FilterNodeFacade 实例就行,
//        // 因为单一个物品的生成, 设计上共用同一个生成上下文.
//        ItemFilterNodeFacade(Path("random/items/filters"))
//    }

    // Components ...
    single<TypeSerializerCollection>(named(ITEM_COMPONENT_TEMPLATE_SERIALIZERS)) {
        ItemTemplateTypes.serializers()
    }
}
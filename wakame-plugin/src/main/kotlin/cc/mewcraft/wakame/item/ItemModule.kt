package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.configurate.MaterialSerializer
import cc.mewcraft.wakame.item.component.componentModule
import cc.mewcraft.wakame.item.components.componentsModule
import cc.mewcraft.wakame.item.template.ITEM_COMPONENT_TEMPLATE_SERIALIZERS
import cc.mewcraft.wakame.item.template.templateModule
import cc.mewcraft.wakame.item.templates.filter.FilterSerializer
import cc.mewcraft.wakame.item.templates.templatesModule
import cc.mewcraft.wakame.item.vanilla.VANILLA_COMPONENT_REMOVER_SERIALIZER
import cc.mewcraft.wakame.item.vanilla.VanillaComponentRemover
import cc.mewcraft.wakame.item.vanilla.vanillaModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ITEM_PROTO_SERIALIZERS = "item_proto_serializers"

internal fun itemModule(): Module = module {
    includes(
        componentModule(),
        componentsModule(),
        templateModule(),
        templatesModule(),
        vanillaModule(),
    )

    // NekoItem 的序列化器
    single<TypeSerializerCollection>(named(ITEM_PROTO_SERIALIZERS)) {
        TypeSerializerCollection.builder()

            // show in tooltip
            .kregister(NaiveShownInTooltipApplicatorSerializer)
            // item slot
            .kregister(ItemSlotSerializer)
            // item type
            .kregister(MaterialSerializer)
            // item filter (for all Pool & Group)
            .kregister(FilterSerializer)
            // vanilla component remover
            .kregister(get<TypeSerializer<VanillaComponentRemover>>(named(VANILLA_COMPONENT_REMOVER_SERIALIZER)))
            // item component templates
            .registerAll(get<TypeSerializerCollection>(named(ITEM_COMPONENT_TEMPLATE_SERIALIZERS)))

            .build()
    }

    single<MultipleItemListener> { MultipleItemListener() }
    single<SingleItemListener> { SingleItemListener() }
}
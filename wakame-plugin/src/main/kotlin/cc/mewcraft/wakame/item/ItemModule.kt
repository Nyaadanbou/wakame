package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.configurate.MaterialSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.component.componentModule
import cc.mewcraft.wakame.item.components.componentsModule
import cc.mewcraft.wakame.item.logic.ItemSlotChangeMonitor
import cc.mewcraft.wakame.item.logic.logicModule
import cc.mewcraft.wakame.item.template.ITEM_COMPONENT_TEMPLATE_SERIALIZERS
import cc.mewcraft.wakame.item.template.templateModule
import cc.mewcraft.wakame.item.templates.templatesModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ITEM_PROTO_SERIALIZERS = "item_proto_serializers"

internal fun itemModule(): Module = module {
    includes(
        componentModule(),
        componentsModule(),
        logicModule(),
        templateModule(),
        templatesModule(),
    )

    single { ItemSlotRegistry } bind Initializable::class
    single { ImaginaryNekoStackRegistry } bind Initializable::class
    single { ImaginaryNekoItemRealizer }
    single { CustomNekoItemRealizer }

    // NekoItem 的序列化器
    single<TypeSerializerCollection>(named(ITEM_PROTO_SERIALIZERS)) {
        TypeSerializerCollection.builder()

            // item type
            .kregister(MaterialSerializer)
            // item slot
            .kregister(ItemSlotSerializer)
            // item slot group
            .kregister(ItemSlotGroupSerializer)
            // item data modifier
            .kregister(ItemBaseSerializer)
            // item component templates
            .registerAll(get(named(ITEM_COMPONENT_TEMPLATE_SERIALIZERS)))

            .build()
    }

    singleOf(::ItemChangeListener)
    singleOf(::ItemBehaviorListener)
    singleOf(::ItemMiscellaneousListener)
    singleOf(::ItemSlotChangeMonitor)
}
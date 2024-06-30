package cc.mewcraft.wakame.item.template

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ITEM_TEMPLATE_SERIALIZERS = "item_template_serializers"

internal fun templateModule(): Module = module {
    single<TypeSerializerCollection>(named(ITEM_TEMPLATE_SERIALIZERS)) {
        ItemTemplateTypes.collectTypeSerializers()
    }
}
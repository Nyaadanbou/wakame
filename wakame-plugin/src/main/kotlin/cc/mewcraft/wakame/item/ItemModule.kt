package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.binary.binaryItemModule
import cc.mewcraft.wakame.item.component.componentModule
import cc.mewcraft.wakame.item.schema.schemaItemModule
import cc.mewcraft.wakame.item.template.templateModule
import cc.mewcraft.wakame.item.vanilla.vanillaModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun itemModule(): Module = module {
    includes(binaryItemModule())
    includes(schemaItemModule())
    includes(componentModule())
    includes(templateModule())
    includes(vanillaModule())

    single<MultipleItemListener> { MultipleItemListener() }
    single<SingleItemListener> { SingleItemListener() }
}
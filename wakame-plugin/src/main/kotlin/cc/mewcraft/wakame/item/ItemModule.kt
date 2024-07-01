package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.binary.binaryItemModule
import cc.mewcraft.wakame.item.component.componentModule
import cc.mewcraft.wakame.item.components.componentsModule
import cc.mewcraft.wakame.item.schema.schemaItemModule
import cc.mewcraft.wakame.item.template.templateModule
import cc.mewcraft.wakame.item.templates.templatesModule
import cc.mewcraft.wakame.item.vanilla.vanillaModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun itemModule(): Module = module {
    includes(
        binaryItemModule(), // FIXME 组件完成后移除
        schemaItemModule(), // FIXME 组件完成后移除
        componentModule(),
        componentsModule(),
        templateModule(),
        templatesModule(),
        vanillaModule(),
    )

    single<MultipleItemListener> { MultipleItemListener() }
    single<SingleItemListener> { SingleItemListener() }
}
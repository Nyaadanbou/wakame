package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.component.componentModule
import cc.mewcraft.wakame.item.components.componentsModule
import cc.mewcraft.wakame.item.logic.logicModule
import cc.mewcraft.wakame.item.template.templateModule
import cc.mewcraft.wakame.item.templates.templatesModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun itemModule(): Module = module {
    includes(
        componentModule(),
        componentsModule(),
        logicModule(),
        templateModule(),
        templatesModule(),
    )
}
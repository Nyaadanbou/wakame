package cc.mewcraft.wakame.item.components.cells.template.cores

import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.templateCoreAttributeModule
import cc.mewcraft.wakame.item.components.cells.template.cores.empty.templateCoreEmptyModule
import cc.mewcraft.wakame.item.components.cells.template.cores.noop.templateCoreNoopModule
import cc.mewcraft.wakame.item.components.cells.template.cores.skill.templateCoreSkillModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun templateCoresModule(): Module = module {
    includes(
        templateCoreAttributeModule(),
        templateCoreEmptyModule(),
        templateCoreNoopModule(),
        templateCoreSkillModule(),
    )
}
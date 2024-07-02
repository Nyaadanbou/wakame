package cc.mewcraft.wakame.item.components.cell.template.cores

import cc.mewcraft.wakame.item.components.cell.template.cores.attribute.templateCoreAttributeModule
import cc.mewcraft.wakame.item.components.cell.template.cores.empty.templateCoreEmptyModule
import cc.mewcraft.wakame.item.components.cell.template.cores.noop.templateCoreNoopModule
import cc.mewcraft.wakame.item.components.cell.template.cores.skill.templateCoreSkillModule
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
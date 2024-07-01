package cc.mewcraft.wakame.item.components.cell.template.cores

import cc.mewcraft.wakame.item.components.cell.template.cores.attribute.coreAttributeModule
import cc.mewcraft.wakame.item.components.cell.template.cores.empty.coreEmptyModule
import cc.mewcraft.wakame.item.components.cell.template.cores.noop.coreNoopModule
import cc.mewcraft.wakame.item.components.cell.template.cores.skill.coreSkillModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun coresModule(): Module = module {
    includes(
        coreAttributeModule(),
        coreEmptyModule(),
        coreNoopModule(),
        coreSkillModule(),
    )
}
package cc.mewcraft.wakame.item.components.cell.cores

import cc.mewcraft.wakame.item.components.cell.cores.attribute.coreAttributeModule
import cc.mewcraft.wakame.item.components.cell.cores.empty.coreEmptyModule
import cc.mewcraft.wakame.item.components.cell.cores.noop.coreNoopModule
import cc.mewcraft.wakame.item.components.cell.cores.skill.coreSkillModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun coreModule(): Module = module {
    includes(
        coreAttributeModule(),
        coreEmptyModule(),
        coreNoopModule(),
        coreSkillModule()
    )
}
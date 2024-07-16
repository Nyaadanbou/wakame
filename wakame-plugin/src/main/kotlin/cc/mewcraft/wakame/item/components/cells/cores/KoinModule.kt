package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.wakame.item.components.cells.cores.attribute.coreAttributeModule
import cc.mewcraft.wakame.item.components.cells.cores.empty.coreEmptyModule
import cc.mewcraft.wakame.item.components.cells.cores.noop.coreNoopModule
import cc.mewcraft.wakame.item.components.cells.cores.skill.coreSkillModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun cellsCoresModule(): Module = module {
    includes(
        coreAttributeModule(),
        coreEmptyModule(),
        coreNoopModule(),
        coreSkillModule()
    )
}
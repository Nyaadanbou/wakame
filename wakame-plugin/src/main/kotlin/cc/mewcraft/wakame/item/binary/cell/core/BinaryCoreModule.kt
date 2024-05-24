package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.wakame.item.binary.cell.core.attribute.attributeCoreModule
import cc.mewcraft.wakame.item.binary.cell.core.empty.emptyCoreModule
import cc.mewcraft.wakame.item.binary.cell.core.noop.noopCoreModule
import cc.mewcraft.wakame.item.binary.cell.core.skill.skillCoreModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun binaryCoreModule(): Module = module {
    includes(
        attributeCoreModule(),
        emptyCoreModule(),
        noopCoreModule(),
        skillCoreModule()
    )
}
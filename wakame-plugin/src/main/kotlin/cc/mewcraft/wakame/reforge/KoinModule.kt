package cc.mewcraft.wakame.reforge

import cc.mewcraft.wakame.reforge.blacksmith.blacksmithModule
import cc.mewcraft.wakame.reforge.merge.mergingModule
import cc.mewcraft.wakame.reforge.mod.moddingModule
import cc.mewcraft.wakame.reforge.recycle.recycleModule
import cc.mewcraft.wakame.reforge.repair.repairModule
import cc.mewcraft.wakame.reforge.reroll.rerollingModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun reforgeModule(): Module = module {
    includes(
        blacksmithModule(),
        mergingModule(),
        moddingModule(),
        rerollingModule(),
        recycleModule(),
        repairModule()
    )
}
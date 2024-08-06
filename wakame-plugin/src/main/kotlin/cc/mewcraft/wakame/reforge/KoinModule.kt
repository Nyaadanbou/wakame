package cc.mewcraft.wakame.reforge

import cc.mewcraft.wakame.reforge.merging.mergingModule
import cc.mewcraft.wakame.reforge.modding.moddingModule
import cc.mewcraft.wakame.reforge.rerolling.rerollingModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun reforgeModule(): Module = module {
    includes(
        mergingModule(),
        moddingModule(),
        rerollingModule()
    )
}
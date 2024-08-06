package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.gui.merging.mergingModule
import cc.mewcraft.wakame.gui.modding.moddingModule
import cc.mewcraft.wakame.gui.rerolling.rerollingModule
import cc.mewcraft.wakame.gui.selling.sellingModule
import org.koin.dsl.module

internal fun guiModule() = module {
    includes(
        mergingModule(),
        moddingModule(),
        rerollingModule(),
        sellingModule()
    )
}
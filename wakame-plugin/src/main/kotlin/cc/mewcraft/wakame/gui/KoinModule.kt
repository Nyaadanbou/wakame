package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.gui.blacksmith.blacksmithModule
import cc.mewcraft.wakame.gui.merge.mergingModule
import cc.mewcraft.wakame.gui.mod.moddingModule
import cc.mewcraft.wakame.gui.reroll.rerollingModule
import org.koin.dsl.module

internal fun guiModule() = module {
    includes(
        mergingModule(),
        moddingModule(),
        rerollingModule(),
        blacksmithModule()
    )
}
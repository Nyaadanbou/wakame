package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.gui.merge.*
import cc.mewcraft.wakame.gui.mod.*
import cc.mewcraft.wakame.gui.recycle.*
import cc.mewcraft.wakame.gui.reroll.*
import org.koin.dsl.*

internal fun guiModule() = module {
    includes(
        mergingModule(),
        moddingModule(),
        rerollingModule(),
        recyclingModule()
    )
}
package cc.mewcraft.wakame.gui.guidebook

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun guidebookModule() = module {
    single { GuideBookRegistry } bind Initializable::class
}
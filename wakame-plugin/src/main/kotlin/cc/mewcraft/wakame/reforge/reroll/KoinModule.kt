package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun rerollingModule(): Module = module {
    single { RerollingTableRegistry } bind Initializable::class
}
package cc.mewcraft.wakame.lang

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun langModule(): Module = module {
    single { GlobalTranslations } bind Initializable::class
}
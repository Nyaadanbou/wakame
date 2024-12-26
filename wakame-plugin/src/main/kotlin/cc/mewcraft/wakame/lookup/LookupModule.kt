package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

internal fun lookupModule(): Module = module {
    single { AssetsLookup } binds arrayOf(Initializable::class)
}
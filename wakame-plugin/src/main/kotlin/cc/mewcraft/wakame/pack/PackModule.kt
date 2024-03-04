package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.pack.generate.ResourcePackManager
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

internal fun packModule(): Module = module {
    single<ResourcePackManager> {
        ResourcePackManager()
    } binds arrayOf(Initializable::class)

    single<ResourcePackListener> {
        ResourcePackListener()
    }
}
package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module

internal fun packModule(): Module = module {
    singleOf(::VanillaResourcePack)

    single<ResourcePackManager> {
        ResourcePackManager()
    } binds arrayOf(Initializable::class)

    single<ResourcePackListener> {
        ResourcePackListener()
    }
}
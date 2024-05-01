package cc.mewcraft.wakame.world

import cc.mewcraft.wakame.world.attribute.damage.DamageListener
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun worldModule(): Module = module {
    single<DamageListener> {
        DamageListener()
    }
}
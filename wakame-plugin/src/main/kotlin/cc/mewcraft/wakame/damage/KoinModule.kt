package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun damageModule(): Module = module {
    single { DamageListener }
    single { VanillaDamageMappings } bind Initializable::class
}
package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.damage.mappings.*
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun damageModule(): Module = module {
    // logic
    single { DamageListener }

    // data
    single { DamageTypeMappings } bind Initializable::class
    single { EntityAttackMappings } bind Initializable::class
    single { ProjectileTypeMappings } bind Initializable::class

    // api
    single { DamageBootstrap } bind Initializable::class
}
package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.damage.mappings.DamageTypeMappings
import cc.mewcraft.wakame.damage.mappings.DirectEntityTypeMappings
import cc.mewcraft.wakame.damage.mappings.EntityAttackMappings
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
    single { DirectEntityTypeMappings } bind Initializable::class

    // api
    single { DamageBootstrap } bind Initializable::class
}
package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.damage.mappings.DamageTypeMappings
import cc.mewcraft.wakame.damage.mappings.EntityAttackMappings
import cc.mewcraft.wakame.damage.mappings.ProjectileTypeMappings
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun damageModule(): Module = module {
    single { DamageListener }
    single { DamageTypeMappings } bind Initializable::class
    single { EntityAttackMappings } bind Initializable::class
    single { ProjectileTypeMappings } bind Initializable::class
}
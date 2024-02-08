package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.instance.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun attributeModule(): Module = module {
    // TODO setup it

    singleOf(::AttackDamage)
    singleOf(::AttackDamageRate)
    singleOf(::AttackEffectChance)
    singleOf(::AttackSpeedLevel)
    singleOf(::CriticalStrikeChance)
    singleOf(::CriticalStrikePower)
    singleOf(::DamageTakenRate)
    singleOf(::Defense)
    singleOf(::DefensePenetration)
    singleOf(::ElementDefense)
    singleOf(::HealthRegeneration)
    singleOf(::Lifesteal)
    singleOf(::LifestealRate)
    singleOf(::ManaConsumptionRate)
    singleOf(::ManaRegeneration)
    singleOf(::Manasteal)
    singleOf(::ManastealRate)
    singleOf(::MaxAbsorption)
    singleOf(::MaxHealth)
    singleOf(::MaxMana)
    singleOf(::MovementSpeedRate)

}
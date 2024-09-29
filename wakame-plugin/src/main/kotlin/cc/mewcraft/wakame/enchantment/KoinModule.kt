package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun enchantmentModule(): Module = module {
    single { EnchantmentInitializer } bind Initializable::class
    singleOf(::EnchantmentEventHandler)
}
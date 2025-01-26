package cc.mewcraft.wakame.integration

import cc.mewcraft.wakame.integration.economy.intrinsics.VanillaEconomyIntegration
import cc.mewcraft.wakame.integration.economy.intrinsics.ZeroEconomyIntegration
import cc.mewcraft.wakame.integration.playerlevel.intrinsics.VanillaLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.intrinsics.ZeroLevelIntegration
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun integrationModule(): Module = module {

    // Economy
    single { ZeroEconomyIntegration }
    single { VanillaEconomyIntegration() }

    // Adventure Level
    single { ZeroLevelIntegration }
    single { VanillaLevelIntegration(get()) }

}
package cc.mewcraft.wakame.integration

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.integration.economy.intrinsics.VanillaEconomyIntegration
import cc.mewcraft.wakame.integration.economy.intrinsics.ZeroEconomyIntegration
import cc.mewcraft.wakame.integration.permission.PermissionManager
import cc.mewcraft.wakame.integration.playerlevel.intrinsics.VanillaLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.intrinsics.ZeroLevelIntegration
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun integrationModule(): Module = module {
    single { HooksLoader } bind Initializable::class
    single { PermissionManager } bind Initializable::class

    single { ZeroEconomyIntegration }
    single { VanillaEconomyIntegration() }

    single { ZeroLevelIntegration }
    single { VanillaLevelIntegration(get()) }
}
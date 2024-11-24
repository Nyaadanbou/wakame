package cc.mewcraft.wakame.economy

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.compatibility.economy.GlobalEconomy
import cc.mewcraft.wakame.integration.economy.EconomyIntegration
import org.koin.dsl.module

internal fun economyModule() = module {
    single<EconomyIntegration> {
        if (NEKO_PLUGIN.isPluginPresent("Economy")) {
            GlobalEconomy
        } else {
            LevelEconomyIntegration
        }
    }
}
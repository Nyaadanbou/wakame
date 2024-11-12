package cc.mewcraft.wakame.economy

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.compatibility.economy.GlobalEconomy
import org.koin.dsl.module

internal fun economyModule() = module {
    single<Economy> {
        if (NEKO_PLUGIN.isPluginPresent("Economy")) {
            GlobalEconomy
        } else {
            LevelEconomy
        }
    }
}
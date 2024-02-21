package cc.mewcraft.wakame.level

import cc.mewcraft.wakame.annotation.InternalApi
import org.bukkit.Server
import org.bukkit.plugin.PluginManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val VANILLA_EXPERIENCE_LEVEL = "vanilla_experience_level"
const val CUSTOM_ADVENTURE_LEVEL = "custom_adventure_level"

internal fun levelModule(): Module = @OptIn(InternalApi::class) module {

    singleOf<PlayerLevelGetter, Server>(::VanillaExperienceLevelGetter) {
        named(VANILLA_EXPERIENCE_LEVEL)
    }

    single<PlayerLevelGetter>(named(CUSTOM_ADVENTURE_LEVEL)) {
        if (get<PluginManager>().isPluginEnabled("AdventureLevel")) {
            new(::CustomAdventureLevelGetter)
        } else {
            NoopPlayerLevelGetter
        }
    }

}
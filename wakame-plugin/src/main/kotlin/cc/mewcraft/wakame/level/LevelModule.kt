package cc.mewcraft.wakame.level

import cc.mewcraft.wakame.initializer.MAIN_CONFIG_NODE
import cc.mewcraft.wakame.util.requireKt
import org.bukkit.plugin.PluginManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.ConfigurationNode

internal fun levelModule(): Module = module {

    // 如此编码将导致 PlayerLevelProvider 无法热重载
    // 但似乎也没有热重载的需求，因此从设计上就不允许热重载
    single<PlayerLevelProvider> {
        val config = get<ConfigurationNode>(named(MAIN_CONFIG_NODE))
        fun takeIfPresent(plugin: String, constructor: () -> PlayerLevelProvider): PlayerLevelProvider {
            return if (get<PluginManager>().isPluginEnabled(plugin)) {
                constructor()
            } else {
                NoopLevelProvider // fallback to noop if required plugin is not enabled on the server
            }
        }

        val type = config.node("player_level_provider").requireKt<PlayerLevelType>()
        when (type) {
            PlayerLevelType.ADVENTURE_LEVEL -> {
                takeIfPresent("AdventureLevel") { new(::AdventureLevelProvider) }
            }

            PlayerLevelType.EXPERIENCE_LEVEL -> {
                new(::VanillaLevelProvider)
            }

            else -> {
                NoopLevelProvider
            }
        }
    }

}
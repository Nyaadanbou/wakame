package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.registry.ABILITY_CONFIG_FILE
import cc.mewcraft.wakame.registry.ABILITY_PROTO_CONFIG_LOADER
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

internal object AbilitySupport : KoinComponent {
    val GLOBAL_STATE_CONFIG by lazy {
        Configs.YAML.build(ABILITY_CONFIG_FILE) {
            defaultOptions { get<YamlConfigurationLoader.Builder>(named((ABILITY_PROTO_CONFIG_LOADER))).defaultOptions() }
        }
    }

    val GLOBAL_STATE_DISPLAY_CONFIG by lazy { GLOBAL_STATE_CONFIG.node("display") }
}
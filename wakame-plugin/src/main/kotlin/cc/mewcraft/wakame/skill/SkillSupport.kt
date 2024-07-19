package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.registry.SKILL_CONFIG_FILE
import cc.mewcraft.wakame.registry.SKILL_PROTO_CONFIG_LOADER
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

internal object SkillSupport : KoinComponent {
    val GLOBAL_SKILL_CONDITIONS: ConfigProvider by lazy {
        Configs.YAML
            .build(SKILL_CONFIG_FILE, false) {
                defaultOptions(get<YamlConfigurationLoader.Builder>(named((SKILL_PROTO_CONFIG_LOADER))).defaultOptions())
            }
            .derive("conditions")
    }
}
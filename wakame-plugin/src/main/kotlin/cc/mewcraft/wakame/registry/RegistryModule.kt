package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.ability.ABILITY_GROUP_SERIALIZERS
import cc.mewcraft.wakame.ability.AbilitySerializer
import cc.mewcraft.wakame.ability.trigger.ABILITY_TRIGGER_SERIALIZERS
import cc.mewcraft.wakame.adventure.ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS
import cc.mewcraft.wakame.util.buildYamlLoader
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

const val ABILITY_CONFIG_ID = "ability"
const val ABILITY_CONFIG_FILE = "$ABILITY_CONFIG_ID.yml"
const val ABILITY_PROTO_CONFIG_DIR = "ability"
const val ABILITY_PROTO_CONFIG_LOADER = "ability_global_config_loader"

internal fun registryModule(): Module = module {
    single<YamlConfigurationLoader.Builder>(named(ABILITY_PROTO_CONFIG_LOADER)) {
        buildYamlLoader {
            register(AbilitySerializer)
            registerAll(get(named(ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS)))
            registerAll(get(named(ABILITY_GROUP_SERIALIZERS)))
            registerAll(get(named(ABILITY_TRIGGER_SERIALIZERS)))
        }
    }
}
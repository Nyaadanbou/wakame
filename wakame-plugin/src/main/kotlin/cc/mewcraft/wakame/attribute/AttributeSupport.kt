package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.registry.ATTRIBUTE_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.ENTITY_GLOBAL_CONFIG_FILE
import org.koin.core.component.KoinComponent

object AttributeSupport : KoinComponent {
    const val ATTRIBUTE_ID_PATTERN_STRING = "[a-z0-9_./]+"

    val GLOBAL_ATTRIBUTE_CONFIG by lazy { Configs.YAML[ATTRIBUTE_GLOBAL_CONFIG_FILE] }
    val ENTITY_ATTRIBUTE_CONFIG by lazy { Configs.YAML[ENTITY_GLOBAL_CONFIG_FILE].derive("entity_attributes") }
}
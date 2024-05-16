package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.registry.ATTRIBUTE_CONFIG_FILE
import cc.mewcraft.wakame.registry.ENTITY_CONFIG_FILE

object AttributeSupport {
    const val ATTRIBUTE_ID_PATTERN_STRING = "[a-z0-9_./]+"

    val GLOBAL_ATTRIBUTE_CONFIG by lazy { Configs.YAML[ATTRIBUTE_CONFIG_FILE] }
    val ENTITY_ATTRIBUTE_CONFIG by lazy { Configs.YAML[ENTITY_CONFIG_FILE].node("entity_attributes") }
}
package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.Namespaces
import net.kyori.adventure.key.Key

//
// 这些常量被广泛用于配置序列化，NBT序列化，以及任何需要唯一识别对象的地方。
//

/**
 * Keys in the `wakame` compound.
 */
object BaseBinaryKeys {
    const val ID = "id"
    const val NAMESPACE = "namespace"
    const val PATH = "path"
    const val VARIANT = "sid"
}

/**
 * Keys in the `reforge` compound.
 */
object ReforgeBinaryKeys {
    const val MOD_COUNT = "mod_count"
    const val REROLL_COUNT = "reroll_count"
}

/**
 * Constant values of Statistics.
 */
object StatisticsConstants {
    const val ENTITY_KILLS = "entity_kills"
    const val PEAK_DAMAGE = "peak_damage"
    const val REFORGE_HISTORY = "reforge_history"

    inline fun createKey(block: StatisticsConstants.() -> String): Key {
        return Key.key(Namespaces.STATISTICS, block(this))
    }
}

/**
 * Constant values of ItemComponent.
 */
object ItemConstants {
    const val ARROW = "arrow"
    const val ATTACK_SPEED = "attack_speed"
    const val ATTRIBUTE_MODIFIERS = "attribute_modifiers"
    const val BOW = "bow"
    const val CAN_BREAK = "can_break"
    const val CAN_PLACE_ON = "can_place_on"
    const val CASTABLE = "castable"
    const val CELLS = "cells"
    const val CRATE = "crate"
    const val CUSTOM_NAME = "custom_name"
    const val CUSTOM_MODEL_DATA = "custom_model_data"
    const val DAMAGE = "damage"
    const val DAMAGEABLE = "damageable"
    const val DYED_COLOR = "dyed_color"
    const val ENCHANTMENTS = "enchantments"
    const val MAX_DAMAGE = "max_damage"
    const val ELEMENTS = "elements"
    const val FIRE_RESISTANT = "fire_resistant"
    const val FOOD = "food"
    const val GLOWABLE = "glowable"
    const val HIDE_TOOLTIP = "hide_tooltip"
    const val HIDE_ADDITIONAL_TOOLTIP = "hide_additional_tooltip"
    const val ITEM_NAME = "item_name"
    const val KIZAMIZ = "kizamiz"
    const val LEVEL = "level"
    const val LORE = "lore"
    const val PORTABLE_CORE = "portable_core"
    const val RARITY = "rarity"
    const val SKIN = "skin"
    const val SKIN_OWNER = "skin_owner"
    const val STANDALONE_CORE = "standalone_core"
    const val STORED_ENCHANTMENTS = "stored_enchantments"
    const val TOOL = "tool"
    const val TRACKABLE = "tracks"
    const val TRIM = "trim"
    const val UNBREAKABLE = "unbreakable"

    inline fun createKey(block: ItemConstants.() -> String): Key {
        return Key.key(Namespaces.ITEM_META, block(this))
    }
}

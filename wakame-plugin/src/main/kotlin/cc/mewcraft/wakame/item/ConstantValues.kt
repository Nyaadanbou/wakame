package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key

//
// 这些常量被广泛用于配置序列化，NBT序列化，以及任何需要唯一识别对象的地方。
//

/**
 * Keys in the `wakame` compound.
 */
object BaseBinaryKeys {
    const val NAMESPACE = "namespace"
    const val PATH = "path"
    const val VARIANT = "sid"
    const val SHOW = "show"
}

/**
 * Keys in the [Cell][CellBinaryKeys.BASE] compound.
 */
object CellBinaryKeys {
    const val BASE = "cell"
    const val REFORGEABLE = "can_reforge"
    const val OVERRIDABLE = "can_override"
}

/**
 * Keys in the [Core][CoreBinaryKeys.BASE] compound.
 */
object CoreBinaryKeys {
    const val BASE = "core"
    const val CORE_IDENTIFIER = "id"
}

/**
 * Keys in the [Curse][CurseBinaryKeys.BASE] compound.
 */
object CurseBinaryKeys {
    const val BASE = "curse"
    const val CURSE_IDENTIFIER = "id"
}

/**
 * Constants values of Curse.
 */
object CurseConstants {
    const val ENTITY_KILLS = "entity_kills"
    const val PEAK_DAMAGE = "peak_damage"

    inline fun createKey(block: CurseConstants.() -> String): Key {
        return Key(Namespaces.CURSE, block(this))
    }
}

/**
 * Keys in the [Reforge][ReforgeBinaryKeys.BASE] compound.
 */
object ReforgeBinaryKeys {
    const val BASE = "reforge"
    const val SUCCESS_COUNT = "success"
    const val FAILURE_COUNT = "failure"
}

/**
 * Keys in the [Statistics][StatisticsBinaryKeys.BASE] compound.
 */
object StatisticsBinaryKeys {
    const val BASE = "statistics"
}

/**
 * Constant values of Statistics.
 */
object StatisticsConstants {
    const val ENTITY_KILLS = "entity_kills"
    const val PEAK_DAMAGE = "peak_damage"
    const val REFORGE_HISTORY = "reforge_history"

    inline fun createKey(block: StatisticsConstants.() -> String): Key {
        return Key(Namespaces.STATISTICS, block(this))
    }
}

/**
 * Constant values of ItemComponent.
 */
object ItemComponentConstants {
    const val ARROW = "arrow"
    const val ATTRIBUTE_MODIFIERS = "attribute_modifiers"
    const val ATTRIBUTABLE = "attributable"
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
    const val HIDE_TOOLTIP = "hide_tooltip"
    const val HIDE_ADDITIONAL_TOOLTIP = "hide_additional_tooltip"
    const val ITEM_NAME = "item_name"
    const val KIZAMIZ = "kizamiz"
    const val KIZAMIABLE = "kizamiable"
    const val LEVEL = "level"
    const val LORE = "lore"
    const val RARITY = "rarity"
    const val SKILLFUL = "skillful"
    const val SKIN = "skin"
    const val SKIN_OWNER = "skin_owner"
    const val STORED_ENCHANTMENTS = "stored_enchantments"
    const val SYSTEM_USE = "system_use"
    const val TOOL = "tool"
    const val TRACKABLE = "tracks"
    const val TRIM = "trim"
    const val UNBREAKABLE = "unbreakable"

    inline fun createKey(block: ItemComponentConstants.() -> String): Key {
        return Key(Namespaces.ITEM_META, block(this))
    }
}

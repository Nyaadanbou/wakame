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
    const val KEY = "key"
    const val SEED = "seed"
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
    const val REFORGE = "reforge"

    inline fun createKey(block: StatisticsConstants.() -> String): Key {
        return Key(Namespaces.STATISTICS, block(this))
    }
}

/**
 * Constant values of ItemMeta.
 */
object ItemMetaConstants {
    const val DISPLAY_LORE = "lore"
    const val DISPLAY_NAME = "name"
    const val DURABILITY = "durability"
    const val ELEMENT = "element"
    const val FOOD = "food"
    const val TOOL = "tool"
    const val KIZAMI = "kizami"
    const val LEVEL = "level"
    const val RARITY = "rarity"
    const val SKIN = "skin"
    const val SKIN_OWNER = "skin_owner"

    inline fun createKey(block: ItemMetaConstants.() -> String): Key {
        return Key(Namespaces.ITEM_META, block(this))
    }
}

package cc.mewcraft.wakame

object NekoTags {
    const val ROOT = "wakame"

    object Root {
        const val NAMESPACE = "ns"
        const val ID = "id"
    }

    object Attribute {
        const val VAL = "val"
        const val MIN = "min"
        const val MAX = "max"
        const val ELEMENT = "elem"
        const val OPERATION = "op"
    }

    object Cell {
        const val ROOT = "cells"
        const val CAN_REFORGE = "can_reforge"
        const val CAN_OVERRIDE = "can_override"
        const val CORE = "core"
        const val CORE_ID = "id"
        const val CURSE = "curse"
        const val CURSE_ID = "id"
        const val REFORGE = "reforge"
    }

    object Meta {
        const val ROOT = "meta"
        const val NAME = "name"
        const val LORE = "lore"
        const val LEVEL = "level"
        const val RARITY = "rarity"
        const val ELEMENT = "element"
        const val KIZAMI = "kizami"
        const val SKIN = "skin"
        const val SKIN_OWNER = "skin_owner"
    }

    object Stats {
        const val ROOT = "stats"
        const val ENTITY_KILLS = "entity_kills"
        const val PEAK_DAMAGE = "peak_damage"
        const val REFORGE = "reforge"
    }

    object Reforge {
        const val SUCCESS = "success"
        const val FAILURE = "failure"
    }
}
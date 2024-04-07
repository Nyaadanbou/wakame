package cc.mewcraft.wakame

object NekoTags {
    const val ROOT = "wakame"

    object Root {
        const val KEY = "key"
        const val SEED = "seed"
        const val SID = "sid"
        const val SHOW = "show"
    }

    object Skill {
        // placeholder constants
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
        const val CORE_KEY = "id"
        const val CURSE = "curse"
        const val CURSE_KEY = "id"
        const val REFORGE = "reforge"
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
package cc.mewcraft.wakame.loot.context

import kotlin.random.Random

interface LootContext {
    companion object {
        val EMPTY: LootContext = object : LootContext {
            override val luck: Float = 0f
            override val random: Random = Random.Default
            override var level: Int = 0
        }
    }

    val random: Random

    val luck: Float

    var level: Int
}
package cc.mewcraft.wakame.loot.context

import kotlin.random.Random

interface LootContext {
    companion object {
        val EMPTY: LootContext = EmptyLootContext
    }

    val random: Random

    val luck: Float

    /**
     * 是否正在迭代整个战利品表的内容.
     *
     * 迭代时会忽略战利品表的条件, 不再具有随机性.
     * 此时的 [cc.mewcraft.wakame.loot.LootPool.rolls] 意为遍历 [cc.mewcraft.wakame.loot.LootPool.rolls] 次战利品表的内容.
     */
    var isIterating: Boolean

    var level: Int
}

private object EmptyLootContext : LootContext {
    override val luck: Float = 0f
    override val random: Random = Random.Default
    override var isIterating: Boolean = false
    override var level: Int = 0
}

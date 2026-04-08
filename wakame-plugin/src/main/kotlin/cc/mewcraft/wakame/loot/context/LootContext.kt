package cc.mewcraft.wakame.loot.context

import kotlin.random.Random

interface LootContext {
    companion object {
        fun default(): LootContext {
            return DefaultLootContext()
        }
    }

    /**
     * 随机数生成器, 用于在战利品表中进行随机抽取.
     */
    val random: Random

    /**
     * 战利品表的幸运值, 用于影响战利品的掉落概率.
     *
     * @see cc.mewcraft.wakame.loot.entry.LootPoolEntry.getWeight
     */
    val luck: Float

    /**
     * 是否选择整个战利品表的内容.
     *
     * 选择时会忽略战利品表的条件, 不再具有随机性.
     * 此时会忽略 [cc.mewcraft.wakame.loot.LootPool.rolls] 属性,
     * 直接让内部逻辑选择所有条目并产出数据.
     */
    var selectEverything: Boolean

    /**
     * 战利品表的抽取等级, 用于影响战利品的掉落概率.
     */
    var level: Int
}

private class DefaultLootContext : LootContext {
    override val luck: Float = 0f
    override val random: Random = Random.Default
    override var selectEverything: Boolean = false
    override var level: Int = 0
}

package cc.mewcraft.wakame.item2.context

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.loot.context.LootContext
import kotlin.random.Random

/**
 * 从 [cc.mewcraft.wakame.item2.KoishItem] 生成一个物品堆叠时的上下文.
 *
 * 该实例的生命周期仅存在于物品生成时. 当一个物品生成完成后该实例便不应该再使用.
 *
 * @property koishItem 物品类型
 */
data class ItemGenerationContext(
    val koishItem: KoishItem,
    override val luck: Float,
    override var level: Int,
): LootContext {
    override val random: Random = Random.Default
}
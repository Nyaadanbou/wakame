package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import kotlin.random.Random

// FIXME #350: 补完
/**
 * 从 [KoishItem] 生成一个物品堆叠时的上下文.
 *
 * 该实例的生命周期仅存在于物品生成时. 当一个物品生成完成后该实例便不应该再使用.
 *
 * @property koishItem 物品类型
 */
data class Context(
    val koishItem: KoishItem,
) : LevelContext {
    constructor(koishItem: RegistryEntry<KoishItem>) : this(koishItem.unwrap())

    val random: Random = Random
    override var level: Int = 1

}

interface LevelContext {
    var level: Int
}
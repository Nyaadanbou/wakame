package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.BinaryCrate
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.scheme.filter.AttributeContextHolder
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random.BasicSelectionContext
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.WatchedPrimitive
import cc.mewcraft.wakame.util.WatchedSet
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import kotlin.random.Random

/**
 * 代表一个物品生成过程的上下文。
 *
 * 该对象的生命周期从 **物品生成开始** 到 **物品生成结束**。对于一个物品来说，物品的生成从头到尾应该只有一个
 * [SchemeGenerationContext] 实例会被创建和使用。
 *
 * 该接口所有函数和变量都是非线程安全！我们暂时不考虑并发。
 */
class SchemeGenerationContext(
    /**
     * 随机数生成器的种子。
     */
    seed: Long = Random.nextLong(),
    /**
     * 触发本次生成的盲盒。
     *
     * 如果是盲盒触发的物品生成，该成员必须不为空。
     */
    val crateObject: BinaryCrate? = null,
    /**
     * 触发本次生成的玩家。
     *
     * 如果是玩家触发的物品生成，该成员必须不为空。
     */
    val playerObject: Player? = null,
) : BasicSelectionContext(seed) {
    /**
     * 已经生成的物品等级。
     */
    var level: Int by WatchedPrimitive(1)

    /**
     * 已经生成的 [Rarity].
     */
    val rarities: MutableCollection<Rarity> by WatchedSet(HashSet()) // 一个物品有且只有一个稀有度，但我们依然用 Set

    /**
     * 已经生成的 [Element].
     */
    val elements: MutableCollection<Element> by WatchedSet(HashSet()) // 一个物品可以有多个元素，因此用 Set

    /**
     * 已经生成的 [Kizami].
     */
    val kizamis: MutableCollection<Kizami> by WatchedSet(HashSet()) // 一个物品可以有多个铭刻，因此用 Set

    /**
     * 已经生成的 [Core].
     */
    val coreKeys: MutableCollection<Key> by WatchedSet(HashSet()) // 一个物品可以有多个词条（当然

    /**
     * 已经生成的 [Curse].
     */
    val curseKeys: MutableCollection<Key> by WatchedSet(HashSet())

    /**
     * 已经生成的 [AttributeContextHolder].
     */
    val attributes: MutableCollection<AttributeContextHolder> by WatchedSet(HashSet())
}

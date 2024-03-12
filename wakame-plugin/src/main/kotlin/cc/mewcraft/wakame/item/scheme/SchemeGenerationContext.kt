package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.scheme.filter.AbilityContextHolder
import cc.mewcraft.wakame.item.scheme.filter.AttributeContextHolder
import cc.mewcraft.wakame.item.scheme.filter.CurseContextHolder
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random.BasicSelectionContext
import cc.mewcraft.wakame.random.SelectionContext
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.WatchedPrimitive
import cc.mewcraft.wakame.util.WatchedSet
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
     * 触发本次物品生成的对象。
     */
    val trigger: SchemaGenerationTrigger,

    /**
     * 随机数生成器的种子。
     *
     * @see SelectionContext.seed
     */
    seed: Long = Random.nextLong(),
) : BasicSelectionContext(seed) {
    /**
     * 已经生成的物品等级。
     */
    var level: Int by WatchedPrimitive(1)

    /**
     * 已经生成的 [Rarity].
     */
    val rarities: MutableCollection<Rarity> by WatchedSet(HashSet())

    /**
     * 已经生成的 [Element].
     */
    val elements: MutableCollection<Element> by WatchedSet(HashSet())

    /**
     * 已经生成的 [Kizami].
     */
    val kizamis: MutableCollection<Kizami> by WatchedSet(HashSet())

    /**
     * 已经生成的 [Curse].
     */
    val curses: MutableCollection<CurseContextHolder> by WatchedSet(HashSet())

    /**
     * 已经生成的 [Ability].
     */
    val abilities: MutableCollection<AbilityContextHolder> by WatchedSet(HashSet())

    /**
     * 已经生成的 [AttributeContextHolder].
     */
    val attributes: MutableCollection<AttributeContextHolder> by WatchedSet(HashSet())
}

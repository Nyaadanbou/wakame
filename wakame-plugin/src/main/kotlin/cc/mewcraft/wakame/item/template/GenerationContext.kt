package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.templates.filter.AttributeContextHolder
import cc.mewcraft.wakame.item.templates.filter.CurseContextHolder
import cc.mewcraft.wakame.item.templates.filter.SkillContextHolder
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random2.SelectionContext
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.WatchedPrimitive
import cc.mewcraft.wakame.util.WatchedReference
import cc.mewcraft.wakame.util.WatchedSet
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

class GenerationContextException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * 代表一个物品生成过程的上下文.
 *
 * 该对象的生命周期从 **物品生成开始** 到 **物品生成结束**. 对于一个物品来说,
 * 物品的生成从头到尾应该只有一个 [GenerationContext] 实例会被创建和使用.
 *
 * 该接口所有函数和变量都是非线程安全! 我们暂时不考虑并发.
 */
class GenerationContext(
    /**
     * 触发本次物品生成的对象.
     */
    val trigger: GenerationTrigger,

    /**
     * 本次生成的物品目标.
     */
    val target: Key,

    /**
     * 随机数生成器的种子.
     */
    seed: Long,
) : SelectionContext(seed) {
    /**
     * 已经生成的物品等级.
     */
    var level: Short? by WatchedPrimitive(null)

    val levelOrThrow: Short
        get() = level ?: throw GenerationContextException("The level is not present in the generation context")

    /**
     * 已经生成的 [Rarity].
     */
    var rarity: Rarity? by WatchedReference(null)

    val rarityOrThrow: Rarity
        get() = rarity ?: throw GenerationContextException("The rarity is not present in the generation context")

    /**
     * 已经生成的 [Element].
     */
    val elements: MutableCollection<Element> by WatchedSet(HashSet())

    /**
     * 已经生成的 [Kizami].
     */
    val kizamiz: MutableCollection<Kizami> by WatchedSet(HashSet())

    /**
     * 已经生成的 [Curse].
     */
    val curses: MutableCollection<CurseContextHolder> by WatchedSet(HashSet())

    /**
     * 已经生成的 [Skill].
     */
    val skills: MutableCollection<SkillContextHolder> by WatchedSet(HashSet())

    /**
     * 已经生成的 [AttributeContextHolder].
     */
    val attributes: MutableCollection<AttributeContextHolder> by WatchedSet(HashSet())

    fun newException(message: String?) {
        // TODO 实现一个可以快速构建 GenerationContextException 的方便函数
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("trigger", trigger),
            ExaminableProperty.of("target", target),
            ExaminableProperty.of("seed", seed),
            ExaminableProperty.of("level", level),
            ExaminableProperty.of("rarity", rarity),
            ExaminableProperty.of("elements", elements),
            ExaminableProperty.of("kizamiz", kizamiz),
            ExaminableProperty.of("curses", curses),
            ExaminableProperty.of("skills", skills),
            ExaminableProperty.of("attributes", attributes),
        )
    }
}
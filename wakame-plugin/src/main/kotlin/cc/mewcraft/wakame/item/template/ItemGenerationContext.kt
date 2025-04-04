package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.attribute.bundle.AttributeGenerationContext
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.random3.Mark
import cc.mewcraft.wakame.random3.RandomSelectorContext
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.ObservableDelegates
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Stream
import kotlin.random.Random


/**
 * 代表一个物品生成的上下文.
 *
 * 该对象的生命周期从 **物品生成开始** 到 **物品生成结束**. 对于一个物品来说,
 * 物品的生成从头到尾应该只有一个 [ItemGenerationContext] 实例会被创建和使用.
 *
 * 该接口所有函数和变量均为非线程安全!
 */
interface ItemGenerationContext : RandomSelectorContext, AttributeGenerationContext {

    // 继承的 RandomSelectorContext
    override val seed: Long
    override val random: Random
    override val marks: MutableCollection<Mark>

    // 继承的 AttributeGenerationContext
    override var level: Int?
    override val attributes: MutableCollection<AttributeContextData>

    /**
     * 本次物品生成的触发器.
     */
    val trigger: ItemGenerationTrigger

    /**
     * 本次生成的目标物品.
     */
    val target: Key

    /**
     * 已经生成的物品稀有度.
     */
    var rarity: RegistryEntry<Rarity>?

    /**
     * 已经生成的物品元素.
     */
    val elements: MutableCollection<RegistryEntry<Element>>

    /**
     * 已经生成的物品铭刻.
     */
    val kizamiz: MutableCollection<RegistryEntry<Kizami>>

    /**
     * 已经生成的物品技能.
     */
    val abilities: MutableCollection<AbilityContextData>
}

/**
 * 提供有关 [ItemGenerationContext] 的静态属性和函数.
 */
object ItemGenerationContexts {
    /**
     * 创建一个新的 [ItemGenerationContext] 实例.
     *
     * @param trigger 本次物品生成的触发器
     * @param target 本次生成的目标物品
     * @param seed 用于生成随机数的种子
     */
    fun create(
        trigger: ItemGenerationTrigger, target: Key, seed: Long = ThreadLocalRandom.current().nextLong(),
    ): ItemGenerationContext {
        return SimpleItemGenerationContext(trigger, target, seed)
    }
}


/* Implementations */


private class SimpleItemGenerationContext(
    override val trigger: ItemGenerationTrigger,
    override val target: Key,
    override val seed: Long,
) : ItemGenerationContext {
    override val random: Random = Random(seed)
    override val marks: MutableCollection<Mark> by ObservableDelegates.set(HashSet())
    override var level: Int? by ObservableDelegates.reference(null)
    override var rarity: RegistryEntry<Rarity>? by ObservableDelegates.reference(null)
    override val elements: MutableCollection<RegistryEntry<Element>> by ObservableDelegates.set(HashSet())
    override val kizamiz: MutableCollection<RegistryEntry<Kizami>> by ObservableDelegates.set(HashSet())
    override val abilities: MutableCollection<AbilityContextData> by ObservableDelegates.set(HashSet())
    override val attributes: MutableCollection<AttributeContextData> by ObservableDelegates.set(HashSet())

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("trigger", trigger),
        ExaminableProperty.of("target", target),
        ExaminableProperty.of("seed", seed),
        ExaminableProperty.of("level", level),
        ExaminableProperty.of("rarity", rarity),
        ExaminableProperty.of("elements", elements),
        ExaminableProperty.of("kizamiz", kizamiz),
        ExaminableProperty.of("ability", abilities),
        ExaminableProperty.of("attributes", attributes),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}
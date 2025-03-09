package cc.mewcraft.wakame.ability.context

import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ability.trigger.TriggerVariant
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import team.unnamed.mocha.MochaEngine
import java.util.stream.Stream

/**
 * 表示了用于释放技能的输入参数.
 */
interface AbilityInput {

    /**
     * 这次技能的施法者.
     */
    val castBy: KoishEntity

    /**
     * 此次技能的目标.
     */
    val targetTo: KoishEntity

    /**
     * 此次技能的触发器 [Trigger], null 表示没有触发器.
     */
    val trigger: Trigger?

    /**
     * 触发此技能的变体.
     */
    val variant: TriggerVariant

    /**
     * 此次技能的施法物品 [NekoStack], null 表示没有施法物品.
     */
    val holdBy: Pair<ItemSlot, NekoStack>?

    /**
     * 此次技能的法力消耗 [Evaluable], 用于计算法力消耗.
     */
    val manaCost: Evaluable<*>

    /**
     * 此次技能的计算引擎 [MochaEngine].
     */
    val mochaEngine: MochaEngine<*>

    fun toBuilder(): AbilityInputDSL
}

/* DSL */

@DslMarker
annotation class AbilityInputMarker

fun abilityInput(caster: KoishEntity, target: KoishEntity, initializer: AbilityInputDSL.() -> Unit = {}): AbilityInput {
    return AbilityInputDSL(caster, target).apply(initializer).build()
}

@AbilityInputMarker
class AbilityInputDSL(
    private val castBy: KoishEntity,
    private val targetTo: KoishEntity,
) {
    private var trigger: Trigger? = null
    private var variant: TriggerVariant = TriggerVariant.any()
    private var holdBy: Pair<ItemSlot, NekoStack>? = null
    private var manaCost: Evaluable<*> = Evaluable.parseNumber(0)
    private var mochaEngine: MochaEngine<*> = MoLangSupport.createEngine()

    fun trigger(trigger: Trigger?): AbilityInputDSL {
        this.trigger = trigger
        return this
    }

    fun variant(variant: TriggerVariant): AbilityInputDSL {
        this.variant = variant
        return this
    }

    fun holdBy(holdBy: Pair<ItemSlot, NekoStack>?): AbilityInputDSL {
        this.holdBy = holdBy
        return this
    }

    fun manaCost(manaCost: Evaluable<*>): AbilityInputDSL {
        this.manaCost = manaCost
        return this
    }

    fun mochaEngine(mochaEngine: MochaEngine<*>): AbilityInputDSL {
        this.mochaEngine = mochaEngine
        return this
    }

    fun build(): AbilityInput = SimpleAbilityInput(
        castBy = castBy,
        targetTo = this@AbilityInputDSL.targetTo,
        trigger = trigger,
        variant = variant,
        holdBy = holdBy,
        manaCost = manaCost,
        mochaEngine = mochaEngine
    )
}

/* Implementations */

private class SimpleAbilityInput(
    override val castBy: KoishEntity,
    override val targetTo: KoishEntity,
    override val trigger: Trigger?,
    override val variant: TriggerVariant,
    override val holdBy: Pair<ItemSlot, NekoStack>?,
    override val manaCost: Evaluable<*>,
    override val mochaEngine: MochaEngine<*>,
) : AbilityInput, Examinable {

    override fun toBuilder(): AbilityInputDSL {
        return AbilityInputDSL(castBy, targetTo)
            .trigger(trigger)
            .holdBy(holdBy)
            .manaCost(manaCost)
            .mochaEngine(mochaEngine)

    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("castBy", castBy),
        ExaminableProperty.of("target", targetTo),
        ExaminableProperty.of("trigger", trigger),
        ExaminableProperty.of("holdBy", holdBy),
        ExaminableProperty.of("manaCost", manaCost),
        ExaminableProperty.of("mochaEngine", mochaEngine),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}
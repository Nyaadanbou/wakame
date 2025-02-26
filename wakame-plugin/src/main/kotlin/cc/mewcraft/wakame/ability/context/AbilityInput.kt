package cc.mewcraft.wakame.ability.context

import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ability.character.Target
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import team.unnamed.mocha.MochaEngine
import java.util.stream.Stream

/**
 * 一次技能执行的上下文.
 */
interface AbilityInput {

    /**
     * 这次技能的施法者 [Caster].
     */
    val castBy: Caster

    /**
     * 此次技能的目标 [Target], 默认为 [castBy] 本身.
     */
    val target: Target

    /**
     * 此次技能的触发器 [Trigger].
     */
    val trigger: Trigger

    /**
     * 如果 [castBy] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    val user: User<*>?

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

fun abilityInput(caster: Caster, initializer: AbilityInputDSL.() -> Unit): AbilityInput {
    return AbilityInputDSL(caster).apply(initializer).build()
}

fun abilityInput(componentMap: ComponentMap): AbilityInput {
    return ComponentMapAbilityInput(componentMap)
}

@AbilityInputMarker
class AbilityInputDSL(
    private val castBy: Caster,
) {
    private var target: Target = TargetAdapter.adapt(castBy)
    private var trigger: Trigger = SingleTrigger.NOOP
    private var holdBy: Pair<ItemSlot, NekoStack>? = null
    private var manaCost: Evaluable<*> = Evaluable.parseNumber(0)
    private var mochaEngine: MochaEngine<*> = MoLangSupport.createEngine()

    fun trigger(trigger: Trigger) = apply { this.trigger = trigger }
    fun target(target: Target) = apply { this.target = target }
    fun holdBy(holdBy: Pair<ItemSlot, NekoStack>?) = apply { this.holdBy = holdBy }
    fun manaCost(manaCost: Evaluable<*>) = apply { this.manaCost = manaCost }
    fun mochaEngine(mochaEngine: MochaEngine<*>) = apply { this.mochaEngine = mochaEngine }

    fun build(): AbilityInput = SimpleAbilityInput(
        castBy = castBy,
        target = target,
        trigger = trigger,
        holdBy = holdBy,
        manaCost = manaCost,
        mochaEngine = mochaEngine
    )
}

/* Implementations */

private class SimpleAbilityInput(
    override val castBy: Caster,
    override val target: Target,
    override val trigger: Trigger,
    override val holdBy: Pair<ItemSlot, NekoStack>?,
    override val manaCost: Evaluable<*>,
    override val mochaEngine: MochaEngine<*>,
) : AbilityInput, Examinable {

    /**
     * 如果 [castBy] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    override val user: User<*>?
        get() = castBy.player?.toUser()

    override fun toBuilder(): AbilityInputDSL {
        return AbilityInputDSL(castBy)
            .trigger(trigger)
            .target(target)
            .holdBy(holdBy)
            .manaCost(manaCost)
            .mochaEngine(mochaEngine)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("castBy", castBy),
        ExaminableProperty.of("target", target),
        ExaminableProperty.of("trigger", trigger),
        ExaminableProperty.of("holdBy", holdBy),
        ExaminableProperty.of("manaCost", manaCost),
        ExaminableProperty.of("mochaEngine", mochaEngine),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

private class ComponentMapAbilityInput(
    private val componentMap: ComponentMap,
) : AbilityInput, Examinable {
    override val castBy: Caster
        get() = requireNotNull(componentMap[CastBy]?.caster) { "Caster not found in componentMap" }
    override val target: Target
        get() = requireNotNull(componentMap[TargetComponent]?.target) { "Target not found in componentMap" }
    override val trigger: Trigger
        get() = requireNotNull(componentMap[TriggerComponent]?.trigger) { "Trigger not found in componentMap" }
    override val user: User<*>?
        get() = castBy.player?.toUser()
    override val holdBy: Pair<ItemSlot, NekoStack>?
        get() {
            val slot = componentMap[HoldBy]?.slot ?: return null
            val nekoStack = componentMap[HoldBy]?.nekoStack ?: return null
            return slot to nekoStack
        }
    override val manaCost: Evaluable<*>
        get() = requireNotNull(componentMap[ManaCostComponent]?.expression) { "ManaCost not found in componentMap" }
    override val mochaEngine: MochaEngine<*>
        get() = requireNotNull(componentMap[MochaEngineComponent]?.mochaEngine) { "MochaEngine not found in componentMap" }

    override fun toBuilder(): AbilityInputDSL {
        return AbilityInputDSL(castBy)
            .trigger(trigger)
            .target(target)
            .holdBy(holdBy)
            .manaCost(manaCost)
            .mochaEngine(mochaEngine)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("castBy", castBy),
        ExaminableProperty.of("target", target),
        ExaminableProperty.of("trigger", trigger),
        ExaminableProperty.of("holdBy", holdBy),
        ExaminableProperty.of("manaCost", manaCost),
        ExaminableProperty.of("mochaEngine", mochaEngine),
    )

    override fun toString(): String {
        return toSimpleString()
    }

    override fun hashCode(): Int {
        return componentMap.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ComponentMapAbilityInput) return false

        return componentMap == other.componentMap
    }
}
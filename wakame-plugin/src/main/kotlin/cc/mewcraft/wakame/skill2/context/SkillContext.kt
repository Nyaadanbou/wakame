package cc.mewcraft.wakame.skill2.context

import cc.mewcraft.wakame.ecs.component.CasterComponent
import cc.mewcraft.wakame.ecs.component.CooldownComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.MochaEngineComponent
import cc.mewcraft.wakame.ecs.component.NekoStackComponent
import cc.mewcraft.wakame.ecs.component.TargetComponent
import cc.mewcraft.wakame.ecs.component.TriggerComponent
import cc.mewcraft.wakame.ecs.data.Cooldown
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.Target
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import team.unnamed.mocha.MochaEngine
import java.util.stream.Stream

/**
 * 一次技能执行的上下文.
 */
interface SkillContext {
    /**
     * 执行此 [cc.mewcraft.wakame.skill2.context.SkillContext] 的技能.
     */
    val skill: Skill

    /**
     * 这次技能的施法者 [Caster].
     */
    val caster: Caster

    /**
     * [skill] 的初始冷却时间.
     */
    val cooldown: Cooldown

    /**
     * 此次技能的触发器 [Trigger].
     */
    val trigger: Trigger

    /**
     * 如果 [caster] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    val user: User<*>?

    /**
     * 此次技能的目标 [Target], null 表示没有目标.
     */
    val target: Target?

    /**
     * 此次技能的施法物品 [NekoStack], null 表示没有施法物品.
     */
    val castItem: NekoStack?

    /**
     * 此次技能的计算引擎 [MochaEngine].
     */
    val mochaEngine: MochaEngine<*>

    fun toBuilder(): SkillContextDSL
}

/* DSL */

@DslMarker
annotation class SkillContextMarker

fun skillContext(skill: Skill, caster: Caster, initializer: SkillContextDSL.() -> Unit): SkillContext {
    return SkillContextDSL(skill, caster).apply(initializer).build()
}

fun skillContext(componentMap: ComponentMap): SkillContext {
    return ComponentMapSkillContext(componentMap)
}

@SkillContextMarker
class SkillContextDSL(
    private val skill: Skill,
    private val caster: Caster,
) {
    private var cooldown: Cooldown = Cooldown(0f)
    private var trigger: Trigger = SingleTrigger.NOOP
    private var target: Target? = null
    private var castItem: NekoStack? = null
    private var mochaEngine: MochaEngine<*> = MoLangSupport.createEngine()

    fun cooldown(cooldown: Cooldown) = apply { this.cooldown = cooldown }
    fun trigger(trigger: Trigger) = apply { this.trigger = trigger }
    fun target(target: Target?) = apply { this.target = target }
    fun castItem(castItem: NekoStack?) = apply { this.castItem = castItem }
    fun mochaEngine(mochaEngine: MochaEngine<*>) = apply { this.mochaEngine = mochaEngine }

    fun build(): SkillContext = SimpleSkillContext(
        skill = skill,
        caster = caster,
        cooldown = cooldown,
        trigger = trigger,
        target = target,
        castItem = castItem,
        mochaEngine = mochaEngine
    )
}

/* Implementations */

private class SimpleSkillContext(
    override val skill: Skill,
    override val caster: Caster,
    override val cooldown: Cooldown,
    override val trigger: Trigger,
    override val target: Target?,
    override val castItem: NekoStack?,
    override val mochaEngine: MochaEngine<*>,
) : SkillContext, Examinable {

    /**
     * 如果 [caster] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    override val user: User<*>?
        get() = caster.player?.toUser()

    override fun toBuilder(): SkillContextDSL {
        return SkillContextDSL(skill, caster)
            .cooldown(cooldown)
            .trigger(trigger)
            .target(target)
            .castItem(castItem)
            .mochaEngine(mochaEngine)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("caster", caster),
        ExaminableProperty.of("cooldown", cooldown),
        ExaminableProperty.of("trigger", trigger),
        ExaminableProperty.of("target", target),
        ExaminableProperty.of("castItem", castItem),
        ExaminableProperty.of("mochaEngine", mochaEngine),
        ExaminableProperty.of("skill", skill),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

private class ComponentMapSkillContext(
    private val componentMap: ComponentMap
) : SkillContext, Examinable {
    override val skill: Skill
        get() = requireNotNull(componentMap[IdentifierComponent]?.id?. let { SkillRegistry.INSTANCES[Key(it)] }) { "Skill not found in componentMap" }
    override val caster: Caster
        get() = requireNotNull(componentMap[CasterComponent]?.caster) { "Caster not found in componentMap" }
    override val cooldown: Cooldown
        get() = requireNotNull(componentMap[CooldownComponent]?.cooldown) { "Cooldown not found in componentMap" }
    override val trigger: Trigger
        get() = requireNotNull(componentMap[TriggerComponent]?.trigger) { "Trigger not found in componentMap" }
    override val user: User<*>?
        get() = caster.player?.toUser()
    override val target: Target?
        get() = componentMap[TargetComponent]?.target
    override val castItem: NekoStack?
        get() = componentMap[NekoStackComponent]?.nekoStack
    override val mochaEngine: MochaEngine<*>
        get() = requireNotNull(componentMap[MochaEngineComponent]?.mochaEngine) { "MochaEngine not found in componentMap" }

    override fun toBuilder(): SkillContextDSL {
        return SkillContextDSL(skill, caster)
            .cooldown(cooldown)
            .trigger(trigger)
            .target(target)
            .castItem(castItem)
            .mochaEngine(mochaEngine)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("skill", skill),
        ExaminableProperty.of("caster", caster),
        ExaminableProperty.of("cooldown", cooldown),
        ExaminableProperty.of("trigger", trigger),
        ExaminableProperty.of("target", target),
        ExaminableProperty.of("castItem", castItem),
        ExaminableProperty.of("mochaEngine", mochaEngine),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}
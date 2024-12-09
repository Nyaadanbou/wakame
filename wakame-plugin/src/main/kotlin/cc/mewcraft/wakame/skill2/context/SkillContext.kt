package cc.mewcraft.wakame.skill2.context

import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.CooldownComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.TriggerComponent
import cc.mewcraft.wakame.ecs.data.Cooldown
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.character.Target
import cc.mewcraft.wakame.skill2.character.toComposite
import cc.mewcraft.wakame.skill2.character.value
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

    val caster: Caster.Composite

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

    val target: Target?

    val castItem: NekoStack?

    val mochaEngine: MochaEngine<*>

    fun toBuilder(): SkillContextDSL
}

/* DSL */

@DslMarker
annotation class SkillContextMarker

fun skillContext(skill: Skill, caster: Caster.Composite, initializer: SkillContextDSL.() -> Unit): SkillContext {
    return SkillContextDSL(skill, caster).apply(initializer).build()
}

fun skillContext(componentMap: ComponentMap): SkillContext {
    return ComponentMapSkillContext(componentMap)
}

@SkillContextMarker
class SkillContextDSL(
    private val skill: Skill,
    private val caster: Caster.Composite,
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
    override val caster: Caster.Composite,
    override val cooldown: Cooldown,
    override val trigger: Trigger,
    override val target: Target?,
    override val castItem: NekoStack?,
    override val mochaEngine: MochaEngine<*>,
    override val skill: Skill,
) : SkillContext, Examinable {

    /**
     * 如果 [caster] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    override val user: User<*>?
        get() = caster.value<Caster.Single.Player>()?.bukkitPlayer?.toUser()

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
    override val caster: Caster.Composite
        get() = requireNotNull(componentMap[BukkitEntityComponent]?.entity?.let { CasterAdapter.adapt(it).toComposite() }) { "Caster not found in componentMap" }
    override val cooldown: Cooldown
        get() = requireNotNull(componentMap[CooldownComponent]?.cooldown) { "Cooldown not found in componentMap" }
    override val trigger: Trigger
        get() = requireNotNull(componentMap[TriggerComponent]?.trigger) { "Trigger not found in componentMap" }
    override val user: User<*>?
        get() = caster.value<Caster.Single.Player>()?.bukkitPlayer?.toUser()
    override val target: Target?
        get() = TODO("Not yet implemented")
    override val castItem: NekoStack?
        get() = TODO("Not yet implemented")
    override val mochaEngine: MochaEngine<*>
        get() = TODO("Not yet implemented")

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
//        ExaminableProperty.of("castItem", castItem),
//        ExaminableProperty.of("mochaEngine", mochaEngine),
//        ExaminableProperty.of("skill", skill),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}
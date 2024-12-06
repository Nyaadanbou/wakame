package cc.mewcraft.wakame.skill2.context

import cc.mewcraft.wakame.ecs.data.Cooldown
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.Target
import cc.mewcraft.wakame.skill2.character.value
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import team.unnamed.mocha.MochaEngine

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
annotation class SkillContextDsl

fun skillContext(initializer: SkillContextDSL.() -> Unit): SkillContext {
    return SkillContextDSL().apply(initializer).build()
}

@SkillContextDsl
class SkillContextDSL {
    private var skill: Skill? = null
    private var caster: Caster.Composite? = null
    private var cooldown: Cooldown = Cooldown(0f)
    private var trigger: Trigger = SingleTrigger.NOOP
    private var target: Target? = null
    private var castItem: NekoStack? = null
    private var mochaEngine: MochaEngine<*> = MoLangSupport.createEngine()

    fun skill(skill: Skill) = apply { this.skill = skill }
    fun caster(caster: Caster.Composite) = apply { this.caster = caster }
    fun cooldown(cooldown: Cooldown) = apply { this.cooldown = cooldown }
    fun trigger(trigger: Trigger) = apply { this.trigger = trigger }
    fun target(target: Target?) = apply { this.target = target }
    fun castItem(castItem: NekoStack?) = apply { this.castItem = castItem }
    fun mochaEngine(mochaEngine: MochaEngine<*>) = apply { this.mochaEngine = mochaEngine }

    fun build(): SkillContext = SimpleSkillContext(
        skill = requireNotNull(skill),
        caster = requireNotNull(caster),
        cooldown = cooldown,
        trigger = trigger,
        target = target,
        castItem = castItem,
        mochaEngine = mochaEngine
    )
}

/* Implementations */

private data class SimpleSkillContext(
    override val caster: Caster.Composite,
    override val cooldown: Cooldown,
    override val trigger: Trigger,
    override val target: Target?,
    override val castItem: NekoStack?,
    override val mochaEngine: MochaEngine<*>,
    override val skill: Skill,
) : SkillContext {

    /**
     * 如果 [caster] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    override val user: User<*>?
        get() = caster.value<Caster.Single.Player>()?.bukkitPlayer?.toUser()

    override fun toBuilder(): SkillContextDSL {
        return SkillContextDSL()
            .skill(skill)
            .caster(caster)
            .cooldown(cooldown)
            .trigger(trigger)
            .target(target)
            .castItem(castItem)
            .mochaEngine(mochaEngine)
    }
}
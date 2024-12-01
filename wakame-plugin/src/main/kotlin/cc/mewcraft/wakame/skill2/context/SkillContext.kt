package cc.mewcraft.wakame.skill2.context

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.Target
import cc.mewcraft.wakame.skill2.character.value
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
     * 如果 [caster] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    val user: User<*>?

    val target: Target?

    val castItem: NekoStack?

    val mochaEngine: MochaEngine<*>
}

fun SkillContext.toMutable(): MutableSkillContext {
    return MutableSkillContext(this)
}

fun SkillContext.toImmutable(): ImmutableSkillContext {
    return ImmutableSkillContext(this)
}

/* Implementations */

data class MutableSkillContext(
    override var caster: Caster.Composite,
    override var target: Target? = null,
    override var castItem: NekoStack? = null,
    override var mochaEngine: MochaEngine<*> = MoLangSupport.createEngine(),
) : SkillContext {

    constructor(context: SkillContext) : this(context.caster, context.target, context.castItem, context.mochaEngine)

    /**
     * 执行此 [cc.mewcraft.wakame.skill2.context.SkillContext] 的技能.
     *
     * 需要由 [Skill] 实现方进行设置.
     */
    override var skill: Skill = Skill.empty()
        private set

    /**
     * 如果 [caster] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    override val user: User<*>?
        get() = caster.value<Caster.Single.Player>()?.bukkitPlayer?.toUser()

    fun setSkill(skill: Skill) {
        if (this.skill != Skill.empty())
            throw IllegalStateException("Skill already set")
        this.skill = skill
    }
}

data class ImmutableSkillContext(
    override val caster: Caster.Composite,
    override val target: Target? = null,
    override val castItem: NekoStack? = null,
    override val mochaEngine: MochaEngine<*> = MoLangSupport.createEngine(),
    override val skill: Skill = Skill.empty(),
) : SkillContext {

    constructor(context: SkillContext) : this(context.caster, context.target, context.castItem, context.mochaEngine, context.skill)

    /**
     * 如果 [caster] 可以转变成一个 [User], 则返回它的 [User] 实例.
     */
    override val user: User<*>?
        get() = caster.value<Caster.Single.Player>()?.bukkitPlayer?.toUser()
}
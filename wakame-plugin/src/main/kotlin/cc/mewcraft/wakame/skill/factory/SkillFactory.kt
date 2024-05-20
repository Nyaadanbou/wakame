package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Skill

/**
 * Represents a factory of a certain skill.
 *
 * How to create a new skill type:
 * 1. Create a new interface that extends [Skill] interface.
 * 2. Create a companion object that implements [SkillFactory] interface with the type of the skill.
 * 3. Implement the [create] method to create a new instance of the skill.
 * 4. Register the skill type in the [SkillRegistry.SKILL_FACTORIES]
 *
 * Example:
 *
 * ```kotlin
 * interface MySkill : Skill {
 *    val myProperty: String
 *
 *    companion object Factory : SkillType<MySkill> {
 *        override fun create(config: ConfigProvider, key: Key): MySkill {
 *            return DefaultMySkill(config.entry("myProperty"))
 *        }
 *    }
 *
 *    private class DefaultMySkill(override val myProperty: String) : MySkill
 * }
 * ```
 *
 * @param T The type of the skill that this skill type creates.
 */
interface SkillFactory<T : Skill> {
    /**
     * Create a new instance of the skill base on a certain skill type
     */
    fun create(config: ConfigProvider): T
}
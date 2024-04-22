package cc.mewcraft.wakame.skill.type

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import net.kyori.adventure.key.Key

/**
 * Represents a skill type, which is a factory of a certain skill.
 *
 * How to create a new skill type:
 * 1. Create a new interface that extends [ConfiguredSkill] interface.
 * 2. Create a companion object that implements [SkillType] interface with the type of the skill.
 * 3. Implement the [create] method to create a new instance of the skill.
 * 4. Register the skill type in the [SkillRegistry.SKILL_TYPES]
 *
 * Example:
 *
 * ```kotlin
 * interface MySkill : Skill {
 *    val myProperty: String
 *
 *    companion object Type : SkillType<MySkill> {
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
interface SkillType<T : ConfiguredSkill> {
    /**
     * Create a new instance of the skill base on a certain skill type
     */
    fun create(config: ConfigProvider, key: Key): T
}
package cc.mewcraft.wakame.skill2.factory

import cc.mewcraft.wakame.skill2.SkillFactories
import cc.mewcraft.wakame.skill2.Skill
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * Represents a factory of a certain skill.
 *
 * How to create a new skill type:
 * 1. Create a new interface that extends [Skill] interface.
 * 2. Create a companion object that implements [SkillFactory] interface with the type of the skill.
 * 3. Implement the [create] method to create a new instance of the skill.
 * 4. Register the skill type in the [SkillFactories]
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
     *
     * @param key The key of the skill. See [Skill.key]
     * @param config The configuration node of the skill.
     */
    fun create(key: Key, config: ConfigurationNode): T
}
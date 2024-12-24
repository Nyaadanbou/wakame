package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.skill2.context.SkillInput
import cc.mewcraft.wakame.skill2.display.SkillDisplay
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

/**
 * Represents a skill "attached" to a player.
 *
 * If a player has skill X, we say that the skill X is attached to that
 * player; By contrast, if the player has no skill, we say that the player
 * has no skill attached.
 */
interface Skill : Keyed, Examinable {

    /**
     * The key of this skill.
     *
     * **Note that the [key] here is specified by the location of the skill
     * configuration file, not the [cc.mewcraft.wakame.skill2.factory.SkillFactories]'s key**,
     * which means that a [cc.mewcraft.wakame.skill2.factory.SkillFactory] can have multiple [Skill].
     *
     * [Skill] will be stored in the SkillRegistry, and the corresponding
     * [Skill] will be found by the [key].
     */
    override val key: Key

    /**
     * The display infos of this skill.
     */
    val displays: SkillDisplay

    /**
     * 使用 [input] 记录技能的信息到 ECS 中.
     *
     * 具体是先调用 [mechanic], 再将返回结果添加到 ECS 中.
     */
    fun recordBy(input: SkillInput)

    /**
     * 返回一个技能执行的结果, 只有执行 [SkillMechanic] 才会真正执行技能逻辑.
     *
     * @see SkillMechanic
     */
    fun mechanic(input: SkillInput): SkillMechanic

    companion object {
        /**
         * An empty skill.
         */
        fun empty(): Skill = EmptySkill
    }
}

private data object EmptySkill : Skill {
    override val key: Key = Key(Namespaces.SKILL, "empty")
    override val displays: SkillDisplay = SkillDisplay.empty()
    override fun recordBy(input: SkillInput) = Unit
    override fun mechanic(input: SkillInput): SkillMechanic = EmptySkillMechanic
}

internal object SkillSerializer : ScalarSerializer<SkillProvider>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): SkillProvider {
        return SkillProvider(Key(obj.toString()))
    }

    override fun serialize(item: SkillProvider, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }
}
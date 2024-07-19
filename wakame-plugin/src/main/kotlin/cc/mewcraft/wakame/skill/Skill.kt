package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.factory.SkillFactory
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
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
interface Skill : Keyed {
    /**
     * The key of this skill.
     *
     * **Note that the [key] here is specified by the location of the skill
     * configuration file, not the [SkillFactories]'s key**,
     * which means that a [SkillFactory] can have multiple [Skill].
     *
     * [Skill] will be stored in the SkillRegistry, and the corresponding
     * [Skill] will be found by the [key].
     */
    override val key: Key

    /**
     * The conditions that must be met in order to cast this skill.
     *
     * @see SkillConditionGroup
     */
    val conditions: SkillConditionGroup

    /**
     * The display infos of this skill.
     */
    val displays: SkillDisplay

    /**
     * 释放该技能.
     */
    fun cast(context: SkillContext): SkillTick<*> = SkillTick.empty()

    companion object {
        /**
         * An empty skill.
         */
        fun empty(): Skill = EmptySkill
    }
}

/**
 * 代表了一个被动技能.
 *
 * 此类仅作为一个标记接口, 用于标记一个技能是被动技能.
 */
interface PassiveSkill : Skill

private data object EmptySkill : Skill {
    override val key: Key = Key(Namespaces.SKILL, "empty")
    override val displays: SkillDisplay = SkillDisplay.empty()
    override val conditions: SkillConditionGroup = SkillConditionGroup.empty()
}

internal object SkillSerializer : ScalarSerializer<Skill>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Skill {
        val string = obj.toString()
        return SkillRegistry.INSTANCES[Key(string)]
    }

    override fun serialize(item: Skill?, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }
}
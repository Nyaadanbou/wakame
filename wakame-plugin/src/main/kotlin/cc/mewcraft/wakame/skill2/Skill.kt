package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill2.context.SkillInput
import cc.mewcraft.wakame.skill2.display.SkillDisplay
import cc.mewcraft.wakame.skill2.result.SkillResult
import cc.mewcraft.wakame.skill2.trigger.TriggerHandleData
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
     * The conditions that must be met in order to cast this skill.
     *
     * @see SkillConditionGroup
     */
    val conditions: SkillConditionGroup

    /**
     * 在触发对 [cc.mewcraft.wakame.skill2.trigger.Trigger] 进行的判断
     */
    val triggerHandleData: TriggerHandleData

    /**
     * The display infos of this skill.
     */
    val displays: SkillDisplay

    /**
     * 返回一个技能执行的结果, 只有执行 [cc.mewcraft.wakame.skill2.result.SkillResult] 才会真正执行技能逻辑.
     *
     * @see cc.mewcraft.wakame.skill2.result.SkillResult
     */
    fun result(context: SkillInput): SkillResult<Skill>

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
    override val conditions: SkillConditionGroup = SkillConditionGroup.empty()
    override val triggerHandleData: TriggerHandleData = TriggerHandleData()
    override fun result(context: SkillInput): SkillResult<Skill> = SkillResult()
}

internal object SkillSerializer : ScalarSerializer<SkillProvider>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): SkillProvider {
        return SkillProvider(Key(obj.toString()))
    }

    override fun serialize(item: SkillProvider, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }
}
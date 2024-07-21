package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.stream.Stream

/**
 * 包含 [Skill] 共同的实现.
 */
abstract class SkillBase(
    override val key: Key,
    private val config: ConfigurationNode,
) : Skill {
    override val displays: SkillDisplay = config.node("displays").get<SkillDisplay>() ?: SkillDisplay.empty()
    override val conditions: SkillConditionGroup = config.node("conditions").get<SkillConditionGroup>() ?: SkillConditionGroup.empty()

    protected inner class TriggerConditionGetter {
        val forbidden: TriggerConditions = config.node("forbidden_triggers").get<TriggerConditions>() ?: TriggerConditions.empty()
        val interrupt: TriggerConditions = config.node("interrupt_triggers").get<TriggerConditions>() ?: TriggerConditions.empty()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("key", key)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
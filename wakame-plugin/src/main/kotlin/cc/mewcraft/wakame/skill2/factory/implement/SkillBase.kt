package cc.mewcraft.wakame.skill2.factory.implement

import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill2.display.SkillDisplay
import cc.mewcraft.wakame.skill2.trigger.TriggerConditions
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
    override val displays: SkillDisplay = config.node("displays").get<SkillDisplay>() ?: SkillDisplay.Companion.empty()
    override val conditions: SkillConditionGroup = config.node("conditions").get<SkillConditionGroup>() ?: SkillConditionGroup.empty()

    protected inner class TriggerConditionGetter {
        /**
         * 在技能执行时无法执行的操作.
         */
        val forbidden: TriggerConditions = config.node("forbidden_triggers").get<TriggerConditions>() ?: TriggerConditions.empty()

        /**
         * 在执行下列操作时会打断技能.
         */
        val interrupt: TriggerConditions = config.node("interrupt_triggers").get<TriggerConditions>() ?: TriggerConditions.Companion.empty()
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
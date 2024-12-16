package cc.mewcraft.wakame.skill2.factory.implement

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill2.context.SkillInput
import cc.mewcraft.wakame.skill2.display.SkillDisplay
import cc.mewcraft.wakame.skill2.trigger.TriggerHandleData
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.stream.Stream

/**
 * 包含 [Skill] 共同的实现.
 */
abstract class SkillBase(
    final override val key: Key,
    config: ConfigurationNode,
) : Skill {
    companion object : KoinComponent {
        private val wakameWorld: WakameWorld by inject()
    }

    override val displays: SkillDisplay = config.node("displays").get<SkillDisplay>() ?: SkillDisplay.Companion.empty()
    override val conditions: SkillConditionGroup = config.node("conditions").get<SkillConditionGroup>() ?: SkillConditionGroup.empty()
    override val triggerHandleData: TriggerHandleData = config.node("triggers").get<TriggerHandleData>() ?: TriggerHandleData()

    override fun cast(input: SkillInput) {
        addMechanic(input)
    }

    /**
     * 添加一个 [Skill] 状态.
     */
    private fun addMechanic(input: SkillInput) {
        wakameWorld.createEntity(key.asString()) {
            it += EntityType.SKILL
            it += CasterComponent(input.caster)
            it += TargetComponent(input.target)
            it += CooldownComponent(input.cooldown)
            input.castItem?.let { castItem -> it += NekoStackComponent(castItem) }
            it += MechanicComponent(mechanic(input))
            it += StatePhaseComponent(StatePhase.IDLE)
            it += TickCountComponent(.0)
            it += TriggerComponent(input.trigger)
            it += MochaEngineComponent(input.mochaEngine)
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("key", key),
            ExaminableProperty.of("displays", displays),
            ExaminableProperty.of("conditions", conditions),
            ExaminableProperty.of("triggerHandleData", triggerHandleData),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkillBase) return false

        return key == other.key
    }
}
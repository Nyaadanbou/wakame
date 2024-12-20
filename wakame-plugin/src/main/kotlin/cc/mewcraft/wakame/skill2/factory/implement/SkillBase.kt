package cc.mewcraft.wakame.skill2.factory.implement

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.MochaEngineComponent
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TargetComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TriggerComponent
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

    /**
     * 添加一个 [Skill] 状态.
     */
    private fun addMechanic(inputProvider: () -> SkillInput) {
        val input = inputProvider()
        wakameWorld.createEntity(key.asString()) {
            it += EntityType.SKILL
            it += Tags.DISPOSABLE
            it += CastBy(input.castBy)
            it += TargetComponent(input.target)
            input.holdBy?.let { castItem -> it += HoldBy(slot = castItem.first, nekoStack = castItem.second) }
            it += MechanicComponent(mechanic(input))
            it += StatePhaseComponent(StatePhase.IDLE)
            it += TickCountComponent(.0)
            it += TriggerComponent(input.trigger)
            it += MochaEngineComponent(input.mochaEngine)
        }
    }

    override fun recordBy(input: SkillInput) {
        addMechanic { input }
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
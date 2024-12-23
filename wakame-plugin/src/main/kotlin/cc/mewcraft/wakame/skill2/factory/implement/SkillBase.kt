package cc.mewcraft.wakame.skill2.factory.implement

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.context.SkillInput
import cc.mewcraft.wakame.skill2.display.SkillDisplay
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

    override val displays: SkillDisplay = config.node("displays").get<SkillDisplay>() ?: SkillDisplay.empty()

    /**
     * 添加一个 [Skill] 状态.
     */
    private fun addMechanic(input: SkillInput) {
        wakameWorld.createEntity(key.asString()) {
            it += EntityType.SKILL
            it += Tags.DISPOSABLE
            it += CastBy(input.castBy)
            HoldBy(input.holdBy)?.let { holdBy -> it += holdBy }
            it += TargetComponent(input.target)
            input.holdBy?.let { castItem -> it += HoldBy(slot = castItem.first, nekoStack = castItem.second.clone()) }
            it += ManaCostComponent(input.manaCost)
            it += MechanicComponent(mechanic(input))
            it += StatePhaseComponent(StatePhase.IDLE)
            it += TickCountComponent(.0)
            it += TriggerComponent(input.trigger)
            it += MochaEngineComponent(input.mochaEngine)
        }
    }

    override fun recordBy(input: SkillInput) {
        addMechanic(input)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("key", key),
            ExaminableProperty.of("displays", displays),
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
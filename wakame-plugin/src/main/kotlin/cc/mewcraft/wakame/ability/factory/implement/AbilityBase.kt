package cc.mewcraft.wakame.ability.factory.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.display.AbilityDisplay
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.ManaCostComponent
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.MochaEngineComponent
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TargetComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TriggerComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.stream.Stream

/**
 * 包含 [Ability] 共同的实现.
 */
abstract class AbilityBase(
    final override val key: Key,
    config: ConfigurationNode,
) : Ability {

    override val displays: AbilityDisplay = config.node("displays").get<AbilityDisplay>() ?: AbilityDisplay.empty()

    /**
     * 添加一个 [Ability] 状态.
     */
    private fun addMechanic(input: AbilityInput) {
        WakameWorld.createEntity(key.asString()) {
            it += EntityType.ABILITY
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

    override fun recordBy(input: AbilityInput) {
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
        if (other !is AbilityBase) return false

        return key == other.key
    }
}
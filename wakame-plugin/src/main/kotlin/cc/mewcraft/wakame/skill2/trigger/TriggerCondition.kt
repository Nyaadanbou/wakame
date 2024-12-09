package cc.mewcraft.wakame.skill2.trigger

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.skill2.state.StateInfo
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * A collection of [SingleTrigger]s that are valid in a certain [StatePhase].
 */
sealed interface TriggerCondition {
    companion object {
        /**
         * An empty [TriggerCondition].
         */
        fun empty(): TriggerCondition = Empty

        /**
         * Creates a [TriggerCondition] from a multimap of [StateInfo] to a list of [SingleTrigger]s.
         */
        fun of(values: Multimap<StatePhase, SingleTrigger>): TriggerCondition = TriggerConditionImpl(values)
    }

    /**
     * A multimap of [StateInfo] to a list of [SingleTrigger]s.
     */
    val values: Multimap<StatePhase, SingleTrigger>

    fun isMatched(state: StatePhase, trigger: SingleTrigger): Boolean {
        return values[state].contains(trigger)
    }

    private data object Empty : TriggerCondition {
        override val values: Multimap<StatePhase, SingleTrigger> = MultimapBuilder.hashKeys()
            .arrayListValues()
            .build()
    }

    private data class TriggerConditionImpl(
        override val values: Multimap<StatePhase, SingleTrigger>
    ) : TriggerCondition
}

/**
 * A serializer for [TriggerCondition].
 *
 * Format:
 * ```yaml
 * multimap:
 *   idle:
 *     - LEFT_CLICK
 *     - RIGHT_CLICK
 *   casting:
 *     - RIGHT_CLICK
 * ```
 */
internal object TriggersConditionsSerializer : SchemaSerializer<TriggerCondition> {
    override fun deserialize(type: Type, node: ConfigurationNode): TriggerCondition {
        val values = MultimapBuilder.hashKeys().arrayListValues().build<StatePhase, SingleTrigger>()
        for ((key, value) in node.childrenMap()) {
            val stateType = StatePhase.entries.find { it.name.equals(key.toString(), ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown state type: $key")

            for (triggerNode in value.childrenList()) {
                val trigger = triggerNode.krequire<SingleTrigger>()
                values.put(stateType, trigger)
            }
        }
        return TriggerCondition.of(values)
    }
}
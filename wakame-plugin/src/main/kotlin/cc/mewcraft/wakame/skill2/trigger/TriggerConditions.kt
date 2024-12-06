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
sealed interface TriggerConditions {
    companion object {
        /**
         * An empty [TriggerConditions].
         */
        fun empty(): TriggerConditions = Empty

        /**
         * Creates a [TriggerConditions] from a multimap of [StateInfo] to a list of [SingleTrigger]s.
         */
        fun of(values: Multimap<StatePhase, SingleTrigger>): TriggerConditions = TriggerConditionsImpl(values)
    }

    /**
     * A multimap of [StateInfo] to a list of [SingleTrigger]s.
     */
    val values: Multimap<StatePhase, SingleTrigger>

    private data object Empty : TriggerConditions {
        override val values: Multimap<StatePhase, SingleTrigger> = MultimapBuilder.hashKeys()
            .arrayListValues()
            .build()
    }

    private data class TriggerConditionsImpl(
        override val values: Multimap<StatePhase, SingleTrigger>
    ) : TriggerConditions
}

/**
 * A serializer for [TriggerConditions].
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
internal object TriggersConditionsSerializer : SchemaSerializer<TriggerConditions> {
    override fun deserialize(type: Type, node: ConfigurationNode): TriggerConditions {
        val values = MultimapBuilder.hashKeys().arrayListValues().build<StatePhase, SingleTrigger>()
        for ((key, value) in node.childrenMap()) {
            val stateType = StatePhase.entries.find { it.name.equals(key.toString(), ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown state type: $key")

            for (triggerNode in value.childrenList()) {
                val trigger = triggerNode.krequire<SingleTrigger>()
                values.put(stateType, trigger)
            }
        }
        return TriggerConditions.of(values)
    }

}
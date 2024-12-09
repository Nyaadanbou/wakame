package cc.mewcraft.wakame.skill2.trigger

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.ecs.data.StatePhase
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type

interface TriggerHandleData {
    /**
     * 在技能执行时无法执行的操作.
     */
    val forbidden: TriggerCondition

    /**
     * 在执行下列操作时会打断技能.
     */
    val interrupt: TriggerCondition

    fun isForbidden(state: StatePhase, trigger: SingleTrigger): Boolean {
        return forbidden.isMatched(state, trigger)
    }

    fun isInterrupt(state: StatePhase, trigger: SingleTrigger): Boolean {
        return interrupt.isMatched(state, trigger)
    }
}


fun TriggerHandleData(): TriggerHandleData {
    return EmptyTriggerHandleData
}

fun TriggerHandleData(
    forbidden: TriggerCondition,
    interrupt: TriggerCondition,
): TriggerHandleData {
    return SimpleTriggerHandleData(forbidden, interrupt)
}

private data object EmptyTriggerHandleData : TriggerHandleData {
    override val forbidden: TriggerCondition = TriggerCondition.empty()
    override val interrupt: TriggerCondition = TriggerCondition.empty()
}

private data class SimpleTriggerHandleData(
    override val forbidden: TriggerCondition,
    override val interrupt: TriggerCondition,
) : TriggerHandleData

internal object TriggerHandleDataSerializer : SchemaSerializer<TriggerHandleData> {
    override fun deserialize(type: Type, node: ConfigurationNode): TriggerHandleData {
        return TriggerHandleData(
            forbidden = node.node("forbidden").get() ?: TriggerCondition.empty(),
            interrupt = node.node("interrupt").get() ?: TriggerCondition.empty(),
        )
    }
}
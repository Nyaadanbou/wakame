package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.condition.ConditionGroup
import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.UnsupportedOperationException
import java.lang.reflect.Type
import java.util.PriorityQueue
import java.util.Queue

interface SkillConditionGroup : ConditionGroup<SkillCastContext> {
    override fun test(context: SkillCastContext): Boolean
    fun cost(context: SkillCastContext)
    fun notifyFailure(context: SkillCastContext, notifyCount: Int = 1)
}

object EmptySkillConditionGroup : SkillConditionGroup {
    override fun test(context: SkillCastContext): Boolean = true
    override fun cost(context: SkillCastContext) = Unit
    override fun notifyFailure(context: SkillCastContext, notifyCount: Int) = Unit
}

class SortedSkillConditionGroup(
    private val conditions: List<SkillCondition>,
) : SkillConditionGroup {
    private val failureConditions: Queue<SkillCondition> = PriorityQueue(compareBy { it.priority })

    override fun test(context: SkillCastContext): Boolean {
        for (condition in conditions) {
            if (!condition.test(context))
                failureConditions.add(condition)
        }

        return failureConditions.isEmpty()
    }

    override fun cost(context: SkillCastContext) {
        conditions.forEach { it.cost(context) }
    }

    override tailrec fun notifyFailure(context: SkillCastContext, notifyCount: Int) {
        if (notifyCount == 0 || failureConditions.isEmpty())
            return
        val condition = failureConditions.poll()
        condition.notifyFailure(context)
        notifyFailure(context, notifyCount - 1)
    }
}

internal object SkillConditionGroupSerializer : TypeSerializer<SkillConditionGroup> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillConditionGroup {
        val skillConditions = node.krequire<List<ConfigurationNode>>()
            .map { listNode ->
                val conditionType = listNode.node("type").krequire<String>()
                val factory = SkillRegistry.CONDITIONS[conditionType]
                factory.provide(NodeConfigProvider(listNode, ""))
            }

        return SortedSkillConditionGroup(skillConditions)
    }

    override fun serialize(type: Type, obj: SkillConditionGroup?, node: ConfigurationNode) {
        throw UnsupportedOperationException("Serialization is not supported for SkillConditionGroup.")
    }
}
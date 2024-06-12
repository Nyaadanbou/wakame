package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.condition.ConditionGroup
import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.PriorityQueue
import java.util.Queue

/**
 * Represents a skill condition group, which contains conditions and costs.
 * It will be initialized when creating [Skill].
 *
 * When triggering a skill, it will check if the conditions are met.
 * If so, the [Skill] will be executed, and all costs inside will be executed as well. Otherwise, it will not be executed.
 */
interface SkillConditionGroup : ConditionGroup<SkillCastContext> {
    val tagResolvers: Array<TagResolver>

    /**
     * Test if all conditions are met.
     */
    override fun test(context: SkillCastContext): Boolean
    fun cost(context: SkillCastContext)
    fun notifyFailure(context: SkillCastContext, notifyCount: Int = 1)
}

data object EmptySkillConditionGroup : SkillConditionGroup {
    override val tagResolvers: Array<TagResolver> = emptyArray()
    override fun test(context: SkillCastContext): Boolean = true
    override fun cost(context: SkillCastContext) = Unit
    override fun notifyFailure(context: SkillCastContext, notifyCount: Int) = Unit
}

class SortedSkillConditionGroup(
    private val conditions: List<SkillCondition>,
) : SkillConditionGroup {
    override val tagResolvers: Array<TagResolver> = conditions.map { it.tagResolver }.toTypedArray()
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

internal object SkillConditionGroupSerializer : SchemaSerializer<SkillConditionGroup> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillConditionGroup {
        val skillConditions = node.krequire<List<ConfigurationNode>>()
            .map { listNode ->
                val conditionType = listNode.node("type").krequire<String>()
                val factory = SkillRegistry.CONDITIONS[conditionType]
                factory.provide(NodeConfigProvider(listNode))
            }

        return SortedSkillConditionGroup(skillConditions)
    }
}
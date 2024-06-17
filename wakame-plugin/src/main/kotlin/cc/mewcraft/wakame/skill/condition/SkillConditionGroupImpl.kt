package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.TreeSet

/**
 * 一个非空的技能条件组.
 */
internal class SkillConditionGroupImpl(
    conditions: Collection<SkillCondition>,
) : KoinComponent, SkillConditionGroup {
    init {
        require(conditions.isNotEmpty()) { "Empty conditions" }
    }

    private val logger: Logger by inject()

    private val children: TreeSet<SkillCondition> = TreeSet(
        Comparator.comparing(SkillCondition::priority) // 按照优先级进行排序
    ).apply { addAll(conditions) }

    override val resolver: TagResolver = TagResolver.resolver(conditions.map { it.resolver })

    override fun newSession(context: SkillCastContext): SkillConditionSession {
        return SessionImpl(children.map { it.newSession(context) })
    }

    private inner class SessionImpl(
        private val children: List<SkillConditionSession>,
    ) : SkillConditionSession {
        override val isSuccess: Boolean = children.all { it.isSuccess }

        override fun onSuccess(context: SkillCastContext) {
            // 所有条件满足, 执行每个条件的 onSuccess
            children.forEach { it.onSuccess(context) }
        }

        override fun onFailure(context: SkillCastContext) {
            // 存在条件不满足, 执行第一个不满足的条件的 onFailure
            val sessionWithFailure = children.firstOrNull { !it.isSuccess }
            if (sessionWithFailure == null) {
                logger.warn("No condition failed but function on_failure() still gets executed")
                return
            }

            sessionWithFailure.onFailure(context)
        }
    }
}

/**
 * 技能条件组的序列化器.
 */
internal object SkillConditionGroupSerializer : SchemaSerializer<SkillConditionGroup> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillConditionGroup {
        val conditions = node.krequire<List<ConfigurationNode>>().map { listNode ->
            val conditionType = listNode.node("type").krequire<String>()
            val conditionFactory = SkillRegistry.CONDITIONS[conditionType]
            conditionFactory.create(NodeConfigProvider(listNode))
        }
        return SkillConditionGroupImpl(conditions)
    }
}
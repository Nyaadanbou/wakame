package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.MultimapBuilder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 一个非空的技能条件组.
 */
internal class SkillConditionGroupImpl(
    conditions: ImmutableMultimap<ConditionPhase, SkillCondition>,
) : KoinComponent, SkillConditionGroup {
    init {
        require(!conditions.isEmpty) { "Empty conditions" }
    }

    private val logger: Logger by inject()

    private val children: Map<ConditionPhase, Collection<SkillCondition>> = conditions.entries()
        .groupBy({ it.key }, { it.value })
        .mapValues { (_, value) -> value.sortedWith(compareBy({ it.priority }, { it.hashCode() })) }

    override fun getResolver(time: ConditionPhase): TagResolver {
        val children = this.children[time] ?: return TagResolver.empty()
        return TagResolver.resolver(children.map { it.resolver })
    }

    override fun newSession(time: ConditionPhase, context: SkillContext): SkillConditionSession {
        val children = this.children[time] ?: return SkillConditionSession.alwaysSuccess()
        return SessionImpl(children.map { it.newSession(context) })
    }

    private inner class SessionImpl(
        private val children: List<SkillConditionSession>,
    ) : SkillConditionSession {
        override val isSuccess: Boolean = children.all { it.isSuccess }

        override fun onSuccess(context: SkillContext) {
            // 所有条件满足, 执行每个条件的 onSuccess
            children.forEach { it.onSuccess(context) }
        }

        override fun onFailure(context: SkillContext) {
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
        val builder = MultimapBuilder.enumKeys(ConditionPhase::class.java).arrayListValues().build<ConditionPhase, SkillCondition>()

        for ((key, value) in node.childrenMap()) {
            val conditionPhase = try {
                ConditionPhase.valueOf(key.toString().uppercase())
            } catch (e: IllegalArgumentException) {
                throw SerializationException(node, type, "Invalid condition phase: $key")
            }
            val conditions = value.krequire<List<ConfigurationNode>>().map { listNode ->
                val conditionType = listNode.node("type").get<String>() ?: throw SerializationException(listNode, type, "Missing condition type")
                val conditionFactory = SkillRegistry.CONDITIONS[conditionType]
                conditionFactory.create(listNode)
            }
            builder.putAll(conditionPhase, conditions)
        }

        return try {
            SkillConditionGroupImpl(ImmutableMultimap.copyOf(builder))
        } catch (t: Throwable) {
            throw SerializationException(node, type, "Failed to create SkillConditionGroup", t)
        }
    }
}
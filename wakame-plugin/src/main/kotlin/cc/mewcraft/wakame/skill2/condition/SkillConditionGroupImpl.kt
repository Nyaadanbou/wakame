package cc.mewcraft.wakame.skill2.condition

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.EnumLookup
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
    conditions: Map<ConditionPhase, Collection<SkillCondition>>
) : KoinComponent, SkillConditionGroup {
    init {
        require(conditions.isNotEmpty()) { "Empty conditions" }
    }

    private val logger: Logger by inject()

    private val children: Map<ConditionPhase, Collection<SkillCondition>> = conditions
        .mapValues { (_, conditions) -> conditions.sortedBy { it.priority } }

    override fun getResolver(time: ConditionPhase): TagResolver {
        val child = this.children[time] ?: return TagResolver.empty()
        return TagResolver.resolver(child.map { it.resolver })
    }

    override fun newSession(time: ConditionPhase, componentMap: ComponentMap): SkillConditionSession {
        val child = this.children[time] ?: return SkillConditionSession.alwaysSuccess()
        return SessionImpl(child.map { it.newSession(componentMap) })
    }

    private inner class SessionImpl(
        private val children: List<SkillConditionSession>,
    ) : SkillConditionSession {
        override val isSuccess: Boolean = children.all { it.isSuccess }

        override fun onSuccess(componentMap: ComponentMap) {
            // 所有条件满足, 执行每个条件的 onSuccess
            children.forEach { it.onSuccess(componentMap) }
        }

        override fun onFailure(componentMap: ComponentMap) {
            // 存在条件不满足, 执行第一个不满足的条件的 onFailure
            val sessionWithFailure = children.firstOrNull { !it.isSuccess }
            if (sessionWithFailure == null) {
                logger.warn("No condition failed but function on_failure() still gets executed")
                return
            }

            sessionWithFailure.onFailure(componentMap)
        }
    }
}

/**
 * 技能条件组的序列化器.
 */
internal object SkillConditionGroupSerializer : SchemaSerializer<SkillConditionGroup> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillConditionGroup {
        val builder = MultimapBuilder.enumKeys(ConditionPhase::class.java).arrayListValues().build<ConditionPhase, SkillCondition>()

        for ((nodeKey, mapChild) in node.childrenMap()) {
            val conditionPhase = try {
                EnumLookup.lookup<ConditionPhase>(nodeKey.toString()).getOrThrow()
            } catch (e: IllegalArgumentException) {
                throw SerializationException(mapChild, type, "Invalid condition phase: $nodeKey")
            }
            val conditions = mapChild.childrenList().map { listChild ->
                val conditionType = listChild.node("type").get<String>() ?: throw SerializationException(listChild, type, "Missing condition type")
                val conditionFactory = SkillRegistry.CONDITIONS[conditionType]
                conditionFactory.create(listChild)
            }
            builder.putAll(conditionPhase, conditions)
        }

        return try {
            SkillConditionGroupImpl(builder.asMap())
        } catch (t: Throwable) {
            throw SerializationException(node, type, "Failed to create SkillConditionGroup", t)
        }
    }
}
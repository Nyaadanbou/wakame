package cc.mewcraft.wakame.item.components.cell.template

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.item.components.cell.Core
import cc.mewcraft.wakame.item.components.cell.template.cores.attribute.TemplateCoreAttribute
import cc.mewcraft.wakame.item.components.cell.template.cores.attribute.element
import cc.mewcraft.wakame.item.components.cell.template.cores.empty.TemplateCoreEmpty
import cc.mewcraft.wakame.item.components.cell.template.cores.noop.TemplateCoreNoop
import cc.mewcraft.wakame.item.components.cell.template.cores.skill.TemplateCoreSkill
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.FilterAttribute
import cc.mewcraft.wakame.item.templates.filter.FilterSkill
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.random2.GroupSerializer
import cc.mewcraft.wakame.random2.Pool
import cc.mewcraft.wakame.random2.PoolSerializer
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 代表一个[核心][Core]的模板.
 */
interface TemplateCore : Keyed, Examinable {
    /**
     * Generates a value from this template.
     *
     * **The generation must correctly populate the [context]!**
     *
     * @param context the generation context
     * @return a new instance of [Core]
     */
    fun generate(context: GenerationContext): Core
}

internal object TemplateCoreSerializer : TypeDeserializer<TemplateCore> {
    override fun deserialize(type: Type, node: ConfigurationNode): TemplateCore {
        val key = node.node("key").krequire<Key>()
        val ret = when {
            /* 技术核心 */
            key == GenericKeys.NOOP -> TemplateCoreNoop
            key == GenericKeys.EMPTY -> TemplateCoreEmpty

            /* 普通核心 */
            key.namespace() == Namespaces.ATTRIBUTE -> TemplateCoreAttribute(node)
            key.namespace() == Namespaces.SKILL -> TemplateCoreSkill(node)

            // 大概是配置文件写错了
            else -> throw IllegalArgumentException("Unknown namespaced key for template core: ${key.namespace()}")
        }
        return ret
    }
}

/**
 * ## Node structure 1 (fallback)
 *
 * ```yaml
 * <node>:
 *   - key: attribute:attack_speed_level
 *     weight: 1
 *     value: 4
 *   - key: attribute:movement_speed
 *     weight: 1
 *     value: 0.4
 *   - key: attribute:defense
 *     weight: 1
 *     value: 23
 *     meta: x1
 * ```
 *
 * ## Node structure 2
 *
 * ```yaml
 * <node>:
 *   filters:
 *     - type: rarity
 *       rarity: rare
 *   entries:
 *     - key: attribute:attack_speed_level
 *       weight: 1
 *       value: 4
 *     - key: attribute:movement_speed
 *       weight: 1
 *       value: 0.4
 *     - key: attribute:defense
 *       weight: 1
 *       value: 23
 *       meta: x1
 * ```
 */
internal object TemplateCorePoolSerializer : PoolSerializer<TemplateCore, GenerationContext>() {
    override fun sampleFactory(node: ConfigurationNode): TemplateCore {
        return node.krequire<TemplateCore>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }

    override fun intrinsicFilters(content: TemplateCore): Filter<GenerationContext> {
        return when (content) {
            // A noop core should always return true
            is TemplateCoreNoop -> Filter.alwaysTrue()

            // An empty core should always return true
            is TemplateCoreEmpty -> Filter.alwaysTrue()

            // By design, an attribute is considered generated
            // if there is already an attribute with all the same
            // key, operation and element in the selection context.
            is TemplateCoreAttribute -> FilterAttribute(true, content.key, content.operation, content.element)

            // By design, a skill is considered generated
            // if there is already a skill with the same key
            // in the selection context, ignoring the trigger.
            is TemplateCoreSkill -> FilterSkill(true, content.key)

            // Throw if we see an unknown schema core type
            else -> throw SerializationException("Can't create intrinsic conditions for unknown template core: ${content}. This is a bug.")
        }
    }

    override fun onPickSample(content: TemplateCore, context: GenerationContext) {
        // context writes are delayed after the template is realized
    }
}

internal object TemplateCoreGroupSerializer : GroupSerializer<TemplateCore, GenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): Pool<TemplateCore, GenerationContext> {
        return node.krequire<Pool<TemplateCore, GenerationContext>>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }
}
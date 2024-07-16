package cc.mewcraft.wakame.item.components.cells.template

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.TemplateCoreAttribute
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.element
import cc.mewcraft.wakame.item.components.cells.template.cores.empty.TemplateCoreEmpty
import cc.mewcraft.wakame.item.components.cells.template.cores.noop.TemplateCoreNoop
import cc.mewcraft.wakame.item.components.cells.template.cores.skill.TemplateCoreSkill
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.ITEM_FILTER_NODE_FACADE
import cc.mewcraft.wakame.item.templates.filter.FilterAttribute
import cc.mewcraft.wakame.item.templates.filter.FilterSkill
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.FilterNodeFacade
import cc.mewcraft.wakame.random3.GroupSerializer
import cc.mewcraft.wakame.random3.NodeContainer
import cc.mewcraft.wakame.random3.Pool
import cc.mewcraft.wakame.random3.PoolSerializer
import cc.mewcraft.wakame.random3.Sample
import cc.mewcraft.wakame.random3.SampleNodeFacade
import cc.mewcraft.wakame.random3.SampleNodeReader
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
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
        val key = node.node("type").krequire<Key>()
        val ret = when {
            // 技术核心
            key == GenericKeys.NOOP -> TemplateCoreNoop
            key == GenericKeys.EMPTY -> TemplateCoreEmpty

            // 普通核心
            key.namespace() == Namespaces.ATTRIBUTE -> TemplateCoreAttribute(node)
            key.namespace() == Namespaces.SKILL -> TemplateCoreSkill(node)

            // 大概是配置文件写错了
            else -> throw IllegalArgumentException("Unknown namespaced key for template core: ${key.namespace()}")
        }
        return ret
    }
}

internal class TemplateCoreSampleNodeReader : KoinComponent, SampleNodeReader<TemplateCore, GenerationContext>() {
    override val sampleValueType: TypeToken<TemplateCore> = typeTokenOf()
    override val filterNodeFacade: FilterNodeFacade<GenerationContext> by inject(named(ITEM_FILTER_NODE_FACADE))
    override fun readData(node: ConfigurationNode): TemplateCore {
        return node.krequire()
    }
}

internal class TemplateCorePool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<TemplateCore, GenerationContext>>,
    override val filters: NodeContainer<Filter<GenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<TemplateCore, GenerationContext>() {
    override fun whenSelect(value: TemplateCore, context: GenerationContext) {
        // context writes are delayed after the template is realized
    }
}

/**
 * ## Node structure 1 (fallback)
 *
 * ```yaml
 * <node>:
 *   - type: attribute:attack_speed_level
 *     weight: 1
 *     value: 4
 *   - type: attribute:movement_speed
 *     weight: 1
 *     value: 0.4
 *   - type: attribute:defense
 *     weight: 1
 *     value: 23
 * ```
 *
 * ## Node structure 2
 *
 * ```yaml
 * <node>:
 *   filters:
 *     - type: item:rarity
 *       rarity: rare
 *   entries:
 *     - type: attribute:attack_speed_level
 *       weight: 1
 *       value: 4
 *     - type: attribute:movement_speed
 *       weight: 1
 *       value: 0.4
 *     - type: attribute:defense
 *       weight: 1
 *       value: 23
 * ```
 */
internal object TemplateCorePoolSerializer : KoinComponent, PoolSerializer<TemplateCore, GenerationContext>() {
    override val sampleNodeFacade: SampleNodeFacade<TemplateCore, GenerationContext> by inject(named(TEMPLATE_CORE_SAMPLE_NODE_FACADE))
    override val filterNodeFacade: FilterNodeFacade<GenerationContext> by inject(named(ITEM_FILTER_NODE_FACADE))

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<TemplateCore, GenerationContext>>,
        filters: NodeContainer<Filter<GenerationContext>>,
        isReplacement: Boolean,
    ): Pool<TemplateCore, GenerationContext> {
        return TemplateCorePool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }

    override fun valueConstructor(node: ConfigurationNode): TemplateCore {
        return node.krequire<TemplateCore>()
    }

    override fun filterConstructor(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }

    override fun intrinsicFilters(value: TemplateCore): Filter<GenerationContext> {
        return when (value) {
            // A noop core should always return true
            is TemplateCoreNoop -> {
                Filter.alwaysTrue()
            }

            // An empty core should always return true
            is TemplateCoreEmpty -> {
                Filter.alwaysTrue()
            }

            // By design, an attribute is considered generated
            // if there is already an attribute with all the same
            // key, operation and element in the selection context.
            is TemplateCoreAttribute -> {
                FilterAttribute(true, value.key, value.operation, value.element)
            }

            // By design, a skill is considered generated
            // if there is already a skill with the same key
            // in the selection context, ignoring the trigger.
            is TemplateCoreSkill -> {
                FilterSkill(true, value.key)
            }

            // Throw if we see an unknown schema core type
            else -> {
                throw SerializationException("Can't create intrinsic conditions for unknown template core: ${value}. This is a bug.")
            }
        }
    }
}

internal object TemplateCoreGroupSerializer : KoinComponent, GroupSerializer<TemplateCore, GenerationContext>() {
    override val filterNodeFacade: FilterNodeFacade<GenerationContext> by inject(named(ITEM_FILTER_NODE_FACADE))

    override fun poolConstructor(node: ConfigurationNode): Pool<TemplateCore, GenerationContext> {
        return node.krequire<Pool<TemplateCore, GenerationContext>>()
    }

    override fun filterConstructor(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }
}
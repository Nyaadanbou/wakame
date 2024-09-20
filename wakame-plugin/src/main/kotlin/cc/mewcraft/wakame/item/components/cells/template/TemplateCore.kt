package cc.mewcraft.wakame.item.components.cells.template

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.TemplateCoreAttribute
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.element
import cc.mewcraft.wakame.item.components.cells.template.cores.empty.TemplateCoreEmpty
import cc.mewcraft.wakame.item.components.cells.template.cores.noop.TemplateCoreNoop
import cc.mewcraft.wakame.item.components.cells.template.cores.skill.TemplateCoreSkill
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.FilterAttribute
import cc.mewcraft.wakame.item.templates.filter.FilterSerializer
import cc.mewcraft.wakame.item.templates.filter.FilterSkill
import cc.mewcraft.wakame.item.templates.filter.ItemFilterNodeFacade
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.GroupSerializer
import cc.mewcraft.wakame.random3.Node
import cc.mewcraft.wakame.random3.NodeContainer
import cc.mewcraft.wakame.random3.NodeFacadeSupport
import cc.mewcraft.wakame.random3.NodeRepository
import cc.mewcraft.wakame.random3.Pool
import cc.mewcraft.wakame.random3.PoolSerializer
import cc.mewcraft.wakame.random3.Sample
import cc.mewcraft.wakame.random3.SampleNodeFacade
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.skill.SKILL_EXTERNALS
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type
import java.nio.file.Path

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

internal object TemplateCoreSerializer : TypeSerializer<TemplateCore> {
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
            else -> throw SerializationException(node, type, "Unknown namespaced key for template core: ${key.namespace()}")
        }
        return ret
    }
}

/**
 * [TemplateCore] 的 [Pool].
 */
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
 *     - type: attribute:movement_speed
 *       weight: 1
 *       value: 0.4
 *     - type: attribute:defense
 *       weight: 1
 *       value: 23
 * ```
 */
internal object TemplateCorePoolSerializer : KoinComponent, PoolSerializer<TemplateCore, GenerationContext>() {
    override val sampleNodeFacade by inject<TemplateCoreSampleNodeFacade>()
    override val filterNodeFacade by inject<ItemFilterNodeFacade>()

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
}

internal object TemplateCoreGroupSerializer : KoinComponent, GroupSerializer<TemplateCore, GenerationContext>() {
    override val filterNodeFacade by inject<ItemFilterNodeFacade>()

    override fun poolConstructor(node: ConfigurationNode): Pool<TemplateCore, GenerationContext> {
        return node.krequire<Pool<TemplateCore, GenerationContext>>()
    }
}

/**
 * 封装了类型 [TemplateCore] 所需要的所有 [Node] 相关的实现.
 */
@PreWorldDependency(
    runBefore = [ElementRegistry::class, KizamiRegistry::class, AttributeRegistry::class],
    runAfter = [ItemRegistry::class],
)
@ReloadDependency(
    runBefore = [ElementRegistry::class, KizamiRegistry::class, AttributeRegistry::class],
    runAfter = [ItemRegistry::class]
)
internal class TemplateCoreSampleNodeFacade(
    override val dataDir: Path,
) : SampleNodeFacade<TemplateCore, GenerationContext>(), Initializable {
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        registerAll(get(named(ELEMENT_EXTERNALS)))
        registerAll(get(named(SKILL_EXTERNALS)))
        kregister(TemplateCoreSerializer)
        kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<TemplateCore, GenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<TemplateCore> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun decodeSampleData(node: ConfigurationNode): TemplateCore {
        return node.krequire<TemplateCore>()
    }

    override fun intrinsicFilters(value: TemplateCore): Collection<Filter<GenerationContext>> {
        val filter = when (value) {
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
        return listOf(filter)
    }

    override fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    override fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}
package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.Node
import cc.mewcraft.wakame.random3.NodeContainer
import cc.mewcraft.wakame.random3.NodeFacadeSupport
import cc.mewcraft.wakame.random3.NodeRepository
import cc.mewcraft.wakame.random3.Pool
import cc.mewcraft.wakame.random3.PoolSerializer
import cc.mewcraft.wakame.random3.Sample
import cc.mewcraft.wakame.random3.SampleNodeFacade
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.collections.takeUnlessEmpty
import java.nio.file.Path
import cc.mewcraft.wakame.item.components.ItemElements as ItemElementsData


data class ItemElements(
    val selector: Pool<Element, ItemGenerationContext>,
) : ItemTemplate<ItemElementsData> {
    override val componentType: ItemComponentType<ItemElementsData> = ItemComponentTypes.ELEMENTS

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemElementsData> {
        val selected = selector.select(context).takeUnlessEmpty() ?: return ItemGenerationResult.empty()
        val elements = ItemElementsData(ObjectArraySet(selected))
        return ItemGenerationResult.of(elements)
    }

    companion object : ItemTemplateBridge<ItemElements> {
        override fun codec(id: String): ItemTemplateType<ItemElements> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemElements>, KoinComponent {
        override val type: TypeToken<ItemElements> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <pool>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemElements {
            return ItemElements(node.krequire<Pool<Element, ItemGenerationContext>>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .registerAll(get(named(ELEMENT_EXTERNALS)))
                .kregister(ElementPoolSerializer)
                .kregister(FilterSerializer) // 凡是随机池都要用到筛选器
                .build()
        }
    }
}

/**
 * 封装了类型 [Element] 所需要的所有 [Node] 相关的实现.
 */
@PreWorldDependency(
    runBefore = [ElementRegistry::class],
    runAfter = [ItemRegistry::class]
)
@ReloadDependency(
    runBefore = [ElementRegistry::class],
    runAfter = [ItemRegistry::class]
)
internal class ElementSampleNodeFacade(
    override val dataDir: Path,
) : SampleNodeFacade<Element, ItemGenerationContext>(), Initializable {
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        kregister(ElementSerializer)
        kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<Element, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<Element> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun decodeSampleData(node: ConfigurationNode): Element {
        return node.node("type").krequire<Element>()
    }

    override fun intrinsicFilters(value: Element): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }

    override fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    override fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}

/**
 * [Element] 的 [Pool].
 */
private data class ElementPool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<Element, ItemGenerationContext>>,
    override val filters: NodeContainer<Filter<ItemGenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<Element, ItemGenerationContext>() {
    override fun whenSelect(value: Element, context: ItemGenerationContext) {
        context.elements += value
    }
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   sample: 2
 *   filters: [ ]
 *   entries:
 *     - type: element:neutral
 *       weight: 2
 *     - type: element:water
 *       weight: 1
 *     - type: element:fire
 *       weight: 1
 *     - type: element:wind
 *       weight: 1
 * ```
 */
private data object ElementPoolSerializer : KoinComponent, PoolSerializer<Element, ItemGenerationContext>() {
    override val sampleNodeFacade: ElementSampleNodeFacade by inject()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<Element, ItemGenerationContext>>,
        filters: NodeContainer<Filter<ItemGenerationContext>>,
        isReplacement: Boolean,
    ): Pool<Element, ItemGenerationContext> {
        return ElementPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

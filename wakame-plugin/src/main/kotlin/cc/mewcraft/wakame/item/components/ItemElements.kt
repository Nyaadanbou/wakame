package cc.mewcraft.wakame.item.components

import cc.mewcraft.commons.collections.mapToByteArray
import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.templates.filter.FilterSerializer
import cc.mewcraft.wakame.item.templates.filter.ItemFilterNodeFacade
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
import cc.mewcraft.wakame.util.getByteArrayOrNull
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path
import java.util.stream.Stream

data class ItemElements(
    /**
     * 所有的元素.
     */
    val elements: Set<Element>,
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<ItemElements>, ItemComponentMeta {
        /**
         * 构建一个 [ItemElements] 的实例.
         */
        fun of(elements: Collection<Element>): ItemElements {
            return ItemElements(ObjectArraySet(elements))
        }

        /**
         * 构建一个 [ItemElements] 的实例.
         */
        fun of(vararg elements: Element): ItemElements {
            return of(elements.toList())
        }

        override fun codec(id: String): ItemComponentType<ItemElements> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.ELEMENTS
        override val tooltipKey: TooltipKey = ItemComponentConstants.createKey { ELEMENTS }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.MergedTooltip = config.MergedTooltip()
    }

    override fun provideTooltipLore(): LoreLine {
        if (!config.showInTooltip) {
            return LoreLine.noop()
        }
        return LoreLine.simple(tooltipKey, tooltip.render(elements, Element::displayName))
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("elements", elements),
    )

    override fun toString(): String {
        return toSimpleString()
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemElements> {

        override fun read(holder: ItemComponentHolder): ItemElements? {
            val elementSet = holder.getTag()
                ?.getByteArrayOrNull(TAG_VALUE)
                ?.mapTo(ObjectArraySet(2), ElementRegistry::getBy)
                ?: return null
            return ItemElements(elements = elementSet)
        }

        override fun write(holder: ItemComponentHolder, value: ItemElements) {
            require(value.elements.isNotEmpty()) { "The set of elements must not be empty" }
            val byteArray = value.elements.mapToByteArray(Element::binaryId)
            holder.getTagOrCreate().putByteArray(TAG_VALUE, byteArray)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    data class Template(
        private val selector: Pool<Element, GenerationContext>,
    ) : ItemTemplate<ItemElements> {
        override val componentType: ItemComponentType<ItemElements> = ItemComponentTypes.ELEMENTS

        override fun generate(context: GenerationContext): GenerationResult<ItemElements> {
            val selected = selector.select(context).takeUnlessEmpty() ?: return GenerationResult.empty()
            val elements = ItemElements(ObjectArraySet(selected))
            return GenerationResult.of(elements)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template>, KoinComponent {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <pool>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            return Template(node.krequire<Pool<Element, GenerationContext>>())
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
 * [Element] 的 [Pool].
 */
private data class ElementPool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<Element, GenerationContext>>,
    override val filters: NodeContainer<Filter<GenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<Element, GenerationContext>() {
    override fun whenSelect(value: Element, context: GenerationContext) {
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
private data object ElementPoolSerializer : KoinComponent, PoolSerializer<Element, GenerationContext>() {
    override val sampleNodeFacade: ElementSampleNodeFacade by inject()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<Element, GenerationContext>>,
        filters: NodeContainer<Filter<GenerationContext>>,
        isReplacement: Boolean,
    ): Pool<Element, GenerationContext> {
        return ElementPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
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
) : SampleNodeFacade<Element, GenerationContext>(), Initializable {
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        kregister(ElementSerializer)
        kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<Element, GenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<Element> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun decodeSampleData(node: ConfigurationNode): Element {
        return node.node("type").krequire<Element>()
    }

    override fun intrinsicFilters(value: Element): Collection<Filter<GenerationContext>> {
        return emptyList()
    }

    override fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    override fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}
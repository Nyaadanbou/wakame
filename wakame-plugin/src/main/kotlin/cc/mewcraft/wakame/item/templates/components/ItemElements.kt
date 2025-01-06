package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.random3.*
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.collections.takeUnlessEmpty
import java.nio.file.Path
import kotlin.io.path.Path
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
@Init(
    stage = InitStage.PRE_WORLD,
    runBefore = [ItemRegistry::class],
    runAfter = [ElementRegistry::class],
)
@Reload(
    runAfter = [ElementRegistry::class],
    runBefore = [ItemRegistry::class],
)
//@PreWorldDependency(
//    runBefore = [ElementRegistry::class],
//    runAfter = [ItemRegistry::class]
//)
//@ReloadDependency(
//    runBefore = [ElementRegistry::class],
//    runAfter = [ItemRegistry::class]
//)
internal object ElementSampleNodeFacade : SampleNodeFacade<Element, ItemGenerationContext>() {
    override val dataDir: Path = Path("random/items/elements")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        kregister(ElementSerializer)
        kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<Element, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<Element> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun decodeSampleData(node: ConfigurationNode): Element {
        return node.node("type").krequire<Element>()
    }

    override fun intrinsicFilters(value: Element): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }

    @InitFun
    fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    private fun onReload() {
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
    override val sampleNodeFacade: ElementSampleNodeFacade = ElementSampleNodeFacade
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

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

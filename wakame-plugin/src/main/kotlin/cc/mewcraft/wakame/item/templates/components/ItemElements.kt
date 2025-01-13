package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.ItemRegistryConfigStorage
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
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.collections.takeUnlessEmpty
import java.nio.file.Path
import kotlin.io.path.Path
import cc.mewcraft.wakame.item.components.ItemElements as DataItemElements


data class ItemElements(
    val selector: Pool<RegistryEntry<ElementType>, ItemGenerationContext>,
) : ItemTemplate<DataItemElements> {
    override val componentType: ItemComponentType<DataItemElements> = ItemComponentTypes.ELEMENTS

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<DataItemElements> {
        val selected = selector.select(context).takeUnlessEmpty() ?: return ItemGenerationResult.empty()
        val elements = DataItemElements(ObjectArraySet(selected))
        return ItemGenerationResult.of(elements)
    }

    companion object : ItemTemplateBridge<ItemElements> {
        override fun codec(id: String): ItemTemplateType<ItemElements> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemElements> {
        override val type: TypeToken<ItemElements> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <pool>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemElements {
            return ItemElements(node.krequire<Pool<RegistryEntry<ElementType>, ItemGenerationContext>>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(ElementPoolSerializer)
                .kregister(FilterSerializer) // 凡是随机池都要用到筛选器
                .build()
        }
    }
}

/**
 * 封装了类型 [ElementType] 所需要的所有 [Node] 相关的实现.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runBefore = [ItemRegistryConfigStorage::class],
)
@Reload(
    runBefore = [ItemRegistryConfigStorage::class],
)
internal object ElementSampleNodeFacade : SampleNodeFacade<RegistryEntry<ElementType>, ItemGenerationContext>() {
    override val dataDir: Path = Path("random/items/elements")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder()
        .kregister(FilterSerializer)
        .build()
    override val repository: NodeRepository<Sample<RegistryEntry<ElementType>, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<RegistryEntry<ElementType>> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    @InitFun
    fun init() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    fun reload() {
        NodeFacadeSupport.reload(this)
    }

    override fun decodeSampleData(node: ConfigurationNode): RegistryEntry<ElementType> {
        return node.node("type").krequire<RegistryEntry<ElementType>>()
    }

    override fun intrinsicFilters(value: RegistryEntry<ElementType>): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }
}

/**
 * [ElementType] 的 [Pool].
 */
private data class ElementPool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<RegistryEntry<ElementType>, ItemGenerationContext>>,
    override val filters: NodeContainer<Filter<ItemGenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<RegistryEntry<ElementType>, ItemGenerationContext>() {
    override fun whenSelect(value: RegistryEntry<ElementType>, context: ItemGenerationContext) {
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
 *     - type: neutral
 *       weight: 2
 *     - type: water
 *       weight: 1
 *     - type: fire
 *       weight: 1
 *     - type: wind
 *       weight: 1
 * ```
 */
private data object ElementPoolSerializer : PoolSerializer<RegistryEntry<ElementType>, ItemGenerationContext>() {
    override val sampleNodeFacade: ElementSampleNodeFacade = ElementSampleNodeFacade
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<RegistryEntry<ElementType>, ItemGenerationContext>>,
        filters: NodeContainer<Filter<ItemGenerationContext>>,
        isReplacement: Boolean,
    ): Pool<RegistryEntry<ElementType>, ItemGenerationContext> {
        return ElementPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

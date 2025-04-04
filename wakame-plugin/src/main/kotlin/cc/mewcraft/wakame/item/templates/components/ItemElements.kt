package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.random3.*
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
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
    val selector: Pool<RegistryEntry<Element>, ItemGenerationContext>,
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
            return ItemElements(node.require<Pool<RegistryEntry<Element>, ItemGenerationContext>>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register<Pool<RegistryEntry<Element>, ItemGenerationContext>>(ElementPoolSerializer)
                .register<Filter<ItemGenerationContext>>(FilterSerializer) // 凡是随机池都要用到筛选器
                .build()
        }
    }
}

/**
 * 封装了类型 [Element] 所需要的所有 [Node] 相关的实现.
 */
@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
internal object ElementSampleNodeFacade : SampleNodeFacade<RegistryEntry<Element>, ItemGenerationContext>() {
    override val dataDir: Path = Path("random/item_element/")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder()
        .register<Filter<ItemGenerationContext>>(FilterSerializer)
        .build()
    override val repository: NodeRepository<Sample<RegistryEntry<Element>, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<RegistryEntry<Element>> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    @InitFun
    fun init() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    fun reload() {
        NodeFacadeSupport.reload(this)
    }

    override fun decodeSampleData(node: ConfigurationNode): RegistryEntry<Element> {
        return node.node("type").require<RegistryEntry<Element>>()
    }

    override fun intrinsicFilters(value: RegistryEntry<Element>): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }
}

/**
 * [Element] 的 [Pool].
 */
private data class ElementPool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<RegistryEntry<Element>, ItemGenerationContext>>,
    override val filters: NodeContainer<Filter<ItemGenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<RegistryEntry<Element>, ItemGenerationContext>() {
    override fun whenSelect(value: RegistryEntry<Element>, context: ItemGenerationContext) {
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
private data object ElementPoolSerializer : PoolSerializer<RegistryEntry<Element>, ItemGenerationContext>() {
    override val sampleNodeFacade: ElementSampleNodeFacade = ElementSampleNodeFacade
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<RegistryEntry<Element>, ItemGenerationContext>>,
        filters: NodeContainer<Filter<ItemGenerationContext>>,
        isReplacement: Boolean,
    ): Pool<RegistryEntry<Element>, ItemGenerationContext> {
        return ElementPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

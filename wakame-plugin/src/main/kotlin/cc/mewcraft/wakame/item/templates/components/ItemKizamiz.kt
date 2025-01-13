package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.core.RegistryEntry
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
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.Group
import cc.mewcraft.wakame.random3.GroupSerializer
import cc.mewcraft.wakame.random3.Node
import cc.mewcraft.wakame.random3.NodeContainer
import cc.mewcraft.wakame.random3.NodeFacadeSupport
import cc.mewcraft.wakame.random3.NodeRepository
import cc.mewcraft.wakame.random3.Pool
import cc.mewcraft.wakame.random3.PoolSerializer
import cc.mewcraft.wakame.random3.Sample
import cc.mewcraft.wakame.random3.SampleNodeFacade
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
import cc.mewcraft.wakame.item.components.ItemKizamiz as ItemKizamizData


data class ItemKizamiz(
    val selector: Group<RegistryEntry<KizamiType>, ItemGenerationContext>,
) : ItemTemplate<ItemKizamizData> {
    override val componentType: ItemComponentType<ItemKizamizData> = ItemComponentTypes.KIZAMIZ

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemKizamizData> {
        val selected = selector.select(context).takeUnlessEmpty() ?: return ItemGenerationResult.empty()
        val kizamiz = ItemKizamizData(ObjectArraySet(selected))
        return ItemGenerationResult.of(kizamiz)
    }

    companion object : ItemTemplateBridge<ItemKizamiz> {
        override fun codec(id: String): ItemTemplateType<ItemKizamiz> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemKizamiz> {
        override val type: TypeToken<ItemKizamiz> = typeTokenOf()

        override fun decode(node: ConfigurationNode): ItemKizamiz {
            return ItemKizamiz(node.krequire<Group<RegistryEntry<KizamiType>, ItemGenerationContext>>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(KizamiPoolSerializer)
                .kregister(KizamiGroupSerializer)
                .kregister(FilterSerializer) // 凡是随机池都要用到筛选器
                .build()
        }
    }
}

/**
 * [KizamiType] 的 [Pool].
 */
private class KizamiPool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<RegistryEntry<KizamiType>, ItemGenerationContext>>,
    override val filters: NodeContainer<Filter<ItemGenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<RegistryEntry<KizamiType>, ItemGenerationContext>() {
    override fun whenSelect(value: RegistryEntry<KizamiType>, context: ItemGenerationContext) {
        context.kizamiz += value
    }
}

/**
 * ## Configuration node structure
 *
 * ```yaml
 * <node>:
 *   sample: 2
 *   filters: [ ]
 *   entries:
 *     - type: wood
 *       weight: 3
 *     - type: iron
 *       weight: 2
 *     - type: gold
 *       weight: 1
 *     - type: diamond
 *       weight: 1
 *     - type: netherite
 *       weight: 1
 * ```
 */
private object KizamiPoolSerializer : PoolSerializer<RegistryEntry<KizamiType>, ItemGenerationContext>() {
    override val sampleNodeFacade: KizamiSampleNodeFacade = KizamiSampleNodeFacade
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<RegistryEntry<KizamiType>, ItemGenerationContext>>,
        filters: NodeContainer<Filter<ItemGenerationContext>>,
        isReplacement: Boolean,
    ): Pool<RegistryEntry<KizamiType>, ItemGenerationContext> {
        return KizamiPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

private object KizamiGroupSerializer : GroupSerializer<RegistryEntry<KizamiType>, ItemGenerationContext>() {
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(node: ConfigurationNode): Pool<RegistryEntry<KizamiType>, ItemGenerationContext> {
        return node.krequire<Pool<RegistryEntry<KizamiType>, ItemGenerationContext>>()
    }
}

/**
 * 封装了类型 [KizamiType] 所需要的所有 [Node] 相关的实现.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runBefore = [ItemRegistryConfigStorage::class],
)
@Reload(
    runBefore = [ItemRegistryConfigStorage::class],
)
internal object KizamiSampleNodeFacade : SampleNodeFacade<RegistryEntry<KizamiType>, ItemGenerationContext>() {
    override val dataDir: Path = Path("random/items/kizamiz")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder()
        .kregister(FilterSerializer)
        .build()
    override val repository: NodeRepository<Sample<RegistryEntry<KizamiType>, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<RegistryEntry<KizamiType>> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    @InitFun
    fun init() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    fun reload() {
        NodeFacadeSupport.reload(this)
    }

    override fun decodeSampleData(node: ConfigurationNode): RegistryEntry<KizamiType> {
        return node.node("type").krequire<RegistryEntry<KizamiType>>()
    }

    override fun intrinsicFilters(value: RegistryEntry<KizamiType>): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }
}
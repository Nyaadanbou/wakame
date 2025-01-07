package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.kizami.KIZAMI_EXTERNALS
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiSerializer
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
import cc.mewcraft.wakame.rarity.RARITY_EXTERNALS
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
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
import cc.mewcraft.wakame.item.components.ItemKizamiz as ItemKizamizData


data class ItemKizamiz(
    val selector: Group<Kizami, ItemGenerationContext>,
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
    ) : ItemTemplateType<ItemKizamiz>, KoinComponent {
        override val type: TypeToken<ItemKizamiz> = typeTokenOf()

        override fun decode(node: ConfigurationNode): ItemKizamiz {
            return ItemKizamiz(node.krequire<Group<Kizami, ItemGenerationContext>>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .registerAll(get(named(RARITY_EXTERNALS)))
                .registerAll(get(named(KIZAMI_EXTERNALS)))
                .kregister(KizamiPoolSerializer)
                .kregister(KizamiGroupSerializer)
                .kregister(FilterSerializer) // 凡是随机池都要用到筛选器
                .build()
        }
    }
}

/**
 * [Kizami] 的 [Pool].
 */
private class KizamiPool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<Kizami, ItemGenerationContext>>,
    override val filters: NodeContainer<Filter<ItemGenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<Kizami, ItemGenerationContext>() {
    override fun whenSelect(value: Kizami, context: ItemGenerationContext) {
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
 *     - type: kizami:wood
 *       weight: 3
 *     - type: kizami:iron
 *       weight: 2
 *     - type: kizami:gold
 *       weight: 1
 *     - type: kizami:diamond
 *       weight: 1
 *     - type: kizami:netherite
 *       weight: 1
 * ```
 */
private object KizamiPoolSerializer : KoinComponent, PoolSerializer<Kizami, ItemGenerationContext>() {
    override val sampleNodeFacade: KizamiSampleNodeFacade = KizamiSampleNodeFacade
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<Kizami, ItemGenerationContext>>,
        filters: NodeContainer<Filter<ItemGenerationContext>>,
        isReplacement: Boolean,
    ): Pool<Kizami, ItemGenerationContext> {
        return KizamiPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

private object KizamiGroupSerializer : KoinComponent, GroupSerializer<Kizami, ItemGenerationContext>() {
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(node: ConfigurationNode): Pool<Kizami, ItemGenerationContext> {
        return node.krequire<Pool<Kizami, ItemGenerationContext>>()
    }
}

/**
 * 封装了类型 [Kizami] 所需要的所有 [Node] 相关的实现.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runBefore = [ItemRegistry::class],
    runAfter = [KizamiRegistry::class]
)
@Reload(
    runBefore = [ItemRegistry::class],
    runAfter = [KizamiRegistry::class]
)
internal object KizamiSampleNodeFacade : SampleNodeFacade<Kizami, ItemGenerationContext>() {
    override val dataDir: Path = Path("random/items/kizamiz")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        kregister(KizamiSerializer)
        kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<Kizami, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<Kizami> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    @InitFun
    fun init() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    fun reload() {
        NodeFacadeSupport.reload(this)
    }

    override fun decodeSampleData(node: ConfigurationNode): Kizami {
        return node.node("type").krequire<Kizami>()
    }

    override fun intrinsicFilters(value: Kizami): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }
}
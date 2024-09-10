package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.kizami.*
import cc.mewcraft.wakame.random3.*
import cc.mewcraft.wakame.rarity.RARITY_EXTERNALS
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.koin.core.component.*
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path
import cc.mewcraft.wakame.item.components.ItemKizamiz as ItemKizamizData


data class ItemKizamiz(
    private val selector: Group<Kizami, ItemGenerationContext>,
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
    override val sampleNodeFacade: KizamiSampleNodeFacade by inject()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

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
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun poolConstructor(node: ConfigurationNode): Pool<Kizami, ItemGenerationContext> {
        return node.krequire<Pool<Kizami, ItemGenerationContext>>()
    }
}

/**
 * 封装了类型 [Kizami] 所需要的所有 [Node] 相关的实现.
 */
@PreWorldDependency(
    runBefore = [KizamiRegistry::class],
    runAfter = [ItemRegistry::class]
)
@ReloadDependency(
    runBefore = [KizamiRegistry::class],
    runAfter = [ItemRegistry::class]
)
internal class KizamiSampleNodeFacade(
    override val dataDir: Path,
) : SampleNodeFacade<Kizami, ItemGenerationContext>(), Initializable {
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        kregister(ElementSerializer)
        kregister(KizamiSerializer)
        kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<Kizami, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<Kizami> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun decodeSampleData(node: ConfigurationNode): Kizami {
        return node.node("type").krequire<Kizami>()
    }

    override fun intrinsicFilters(value: Kizami): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }

    override fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    override fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}
package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.kizami2.Kizami
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
import cc.mewcraft.wakame.item.components.ItemKizamiz as ItemKizamizData


data class ItemKizamiz(
    val selector: Group<RegistryEntry<Kizami>, ItemGenerationContext>,
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
            return ItemKizamiz(node.require<Group<RegistryEntry<Kizami>, ItemGenerationContext>>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register<Pool<RegistryEntry<Kizami>, ItemGenerationContext>>(KizamiPoolSerializer)
                .register<Group<RegistryEntry<Kizami>, ItemGenerationContext>>(KizamiGroupSerializer)
                .register<Filter<ItemGenerationContext>>(FilterSerializer) // 凡是随机池都要用到筛选器
                .build()
        }
    }
}

/**
 * [Kizami] 的 [Pool].
 */
private class KizamiPool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<RegistryEntry<Kizami>, ItemGenerationContext>>,
    override val filters: NodeContainer<Filter<ItemGenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<RegistryEntry<Kizami>, ItemGenerationContext>() {
    override fun whenSelect(value: RegistryEntry<Kizami>, context: ItemGenerationContext) {
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
private object KizamiPoolSerializer : PoolSerializer<RegistryEntry<Kizami>, ItemGenerationContext>() {
    override val sampleNodeFacade: KizamiSampleNodeFacade = KizamiSampleNodeFacade
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<RegistryEntry<Kizami>, ItemGenerationContext>>,
        filters: NodeContainer<Filter<ItemGenerationContext>>,
        isReplacement: Boolean,
    ): Pool<RegistryEntry<Kizami>, ItemGenerationContext> {
        return KizamiPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

private object KizamiGroupSerializer : GroupSerializer<RegistryEntry<Kizami>, ItemGenerationContext>() {
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(node: ConfigurationNode): Pool<RegistryEntry<Kizami>, ItemGenerationContext> {
        return node.require<Pool<RegistryEntry<Kizami>, ItemGenerationContext>>()
    }
}

/**
 * 封装了类型 [Kizami] 所需要的所有 [Node] 相关的实现.
 */
@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
internal object KizamiSampleNodeFacade : SampleNodeFacade<RegistryEntry<Kizami>, ItemGenerationContext>() {
    override val dataDir: Path = Path("random/item_kizami/")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder()
        .register<Filter<ItemGenerationContext>>(FilterSerializer)
        .build()
    override val repository: NodeRepository<Sample<RegistryEntry<Kizami>, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<RegistryEntry<Kizami>> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    @InitFun
    fun init() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    fun reload() {
        NodeFacadeSupport.reload(this)
    }

    override fun decodeSampleData(node: ConfigurationNode): RegistryEntry<Kizami> {
        return node.node("type").require<RegistryEntry<Kizami>>()
    }

    override fun intrinsicFilters(value: RegistryEntry<Kizami>): Collection<Filter<ItemGenerationContext>> {
        return emptyList()
    }
}
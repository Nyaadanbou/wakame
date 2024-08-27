package cc.mewcraft.wakame.item.components

import cc.mewcraft.commons.collections.mapToByteArray
import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.display2.RendererSystemName
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.ItemConstants
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
import cc.mewcraft.wakame.util.getByteArrayOrNull
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path

data class ItemKizamiz(
    /**
     * 所有的铭刻.
     */
    val kizamiz: Set<Kizami>,
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<ItemKizamiz>, ItemComponentMeta {
        /**
         * 构建一个 [ItemKizamiz] 的实例.
         */
        fun of(kizamiz: Collection<Kizami>): ItemKizamiz {
            return ItemKizamiz(ObjectArraySet(kizamiz))
        }

        /**
         * 构建一个 [ItemKizamiz] 的实例.
         */
        fun of(vararg kizamiz: Kizami): ItemKizamiz {
            return of(kizamiz.toList())
        }

        override fun codec(id: String): ItemComponentType<ItemKizamiz> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.KIZAMIZ
        override val tooltipKey: TooltipKey = ItemConstants.createKey { KIZAMIZ }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.MergedTooltip = config.MergedTooltip()
    }

    override fun provideTooltipLore(systemName: RendererSystemName): LoreLine {
        if (!config.showInTooltip) {
            return LoreLine.noop()
        }
        val rendered = tooltip.render(systemName, kizamiz, Kizami::displayName) ?: return LoreLine.noop()
        return LoreLine.simple(tooltipKey, rendered)
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemKizamiz> {
        override fun read(holder: ItemComponentHolder): ItemKizamiz? {
            val tag = holder.getTag() ?: return null
            val kizamiSet = tag.getByteArrayOrNull(TAG_VALUE)?.mapTo(ObjectArraySet(4), KizamiRegistry::getBy) ?: return null
            return ItemKizamiz(kizamiz = kizamiSet)
        }

        override fun write(holder: ItemComponentHolder, value: ItemKizamiz) {
            require(value.kizamiz.isNotEmpty()) { "The set of kizami must be not empty" }
            holder.editTag { tag ->
                val byteArray = value.kizamiz.mapToByteArray(Kizami::binaryId)
                tag.putByteArray(TAG_VALUE, byteArray)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    data class Template(
        private val selector: Group<Kizami, GenerationContext>,
    ) : ItemTemplate<ItemKizamiz> {
        override val componentType: ItemComponentType<ItemKizamiz> = ItemComponentTypes.KIZAMIZ

        override fun generate(context: GenerationContext): GenerationResult<ItemKizamiz> {
            val selected = selector.select(context).takeUnlessEmpty() ?: return GenerationResult.empty()
            val kizamiz = ItemKizamiz(ObjectArraySet(selected))
            return GenerationResult.of(kizamiz)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template>, KoinComponent {
        override val type: TypeToken<Template> = typeTokenOf()

        override fun decode(node: ConfigurationNode): Template {
            return Template(node.krequire<Group<Kizami, GenerationContext>>())
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
    override val samples: NodeContainer<Sample<Kizami, GenerationContext>>,
    override val filters: NodeContainer<Filter<GenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<Kizami, GenerationContext>() {
    override fun whenSelect(value: Kizami, context: GenerationContext) {
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
private object KizamiPoolSerializer : KoinComponent, PoolSerializer<Kizami, GenerationContext>() {
    override val sampleNodeFacade: KizamiSampleNodeFacade by inject()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<Kizami, GenerationContext>>,
        filters: NodeContainer<Filter<GenerationContext>>,
        isReplacement: Boolean,
    ): Pool<Kizami, GenerationContext> {
        return KizamiPool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

private object KizamiGroupSerializer : KoinComponent, GroupSerializer<Kizami, GenerationContext>() {
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun poolConstructor(node: ConfigurationNode): Pool<Kizami, GenerationContext> {
        return node.krequire<Pool<Kizami, GenerationContext>>()
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
) : SampleNodeFacade<Kizami, GenerationContext>(), Initializable {
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        kregister(ElementSerializer)
        kregister(KizamiSerializer)
        kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<Kizami, GenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<Kizami> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun decodeSampleData(node: ConfigurationNode): Kizami {
        return node.node("type").krequire<Kizami>()
    }

    override fun intrinsicFilters(value: Kizami): Collection<Filter<GenerationContext>> {
        return emptyList()
    }

    override fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    override fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}
package cc.mewcraft.wakame.item.components.cells.template

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.template.curses.TemplateCurseEmpty
import cc.mewcraft.wakame.item.components.cells.template.curses.TemplateCurseEntityKills
import cc.mewcraft.wakame.item.components.cells.template.curses.TemplateCursePeakDamage
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.CurseContextHolder
import cc.mewcraft.wakame.item.templates.filter.FilterSerializer
import cc.mewcraft.wakame.item.templates.filter.ItemFilterNodeFacade
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.GroupSerializer
import cc.mewcraft.wakame.random3.Node
import cc.mewcraft.wakame.random3.NodeContainer
import cc.mewcraft.wakame.random3.NodeFacadeSupport
import cc.mewcraft.wakame.random3.NodeRepository
import cc.mewcraft.wakame.random3.Pool
import cc.mewcraft.wakame.random3.PoolSerializer
import cc.mewcraft.wakame.random3.Sample
import cc.mewcraft.wakame.random3.SampleNodeFacade
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type
import java.nio.file.Path

/**
 * 代表一个[诅咒][Curse]的模板.
 */
interface TemplateCurse : Keyed, Examinable {

    /**
     * 用于序列化确定诅咒的类型.
     */
    override val key: Key

    /**
     * Generates a value from this template.
     *
     * **The generation must correctly populate the [context]!**
     *
     * @param context the generation context
     * @return a new instance of [Core]
     */
    fun generate(context: GenerationContext): Curse
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   key: <key>
 *   ...
 * ```
 */
internal object TemplateCurseSerializer : TypeDeserializer<TemplateCurse> {
    private val KEY_ENTITY_KILLS = CurseConstants.createKey { ENTITY_KILLS }
    private val KEY_PEAK_DAMAGE = CurseConstants.createKey { PEAK_DAMAGE }

    override fun deserialize(type: Type, node: ConfigurationNode): TemplateCurse {
        val ret = when (val key = node.node("type").krequire<Key>()) {
            // 技术诅咒
            GenericKeys.EMPTY -> TemplateCurseEmpty

            // 普通诅咒
            KEY_ENTITY_KILLS -> TemplateCurseEntityKills(node)
            KEY_PEAK_DAMAGE -> TemplateCursePeakDamage(node)

            // 大概是配置文件写错了
            else -> throw IllegalArgumentException("Unknown curse: $key")
        }
        return ret
    }
}

@PreWorldDependency(
    runBefore = [ElementRegistry::class, EntityRegistry::class],
    runAfter = [ItemRegistry::class]
)
@ReloadDependency(
    runBefore = [ElementRegistry::class, EntityRegistry::class],
    runAfter = [ItemRegistry::class]
)
/**
 * 封装了类型 [TemplateCurse] 所需要的所有 [Node] 相关的实现.
 */
internal class TemplateCurseSampleNodeFacade(
    override val dataDir: Path,
) : SampleNodeFacade<TemplateCurse, GenerationContext>(), Initializable {
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        registerAll(get(named(ELEMENT_EXTERNALS)))
            .registerAll(get(named(ENTITY_TYPE_HOLDER_EXTERNALS)))
            .kregister(TemplateCurseSerializer)
            .kregister(FilterSerializer)
    }.build()
    override val repository: NodeRepository<Sample<TemplateCurse, GenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<TemplateCurse> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()
    override fun decodeSampleData(node: ConfigurationNode): TemplateCurse {
        return node.krequire<TemplateCurse>()
    }

    override fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    override fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}

/**
 * [TemplateCurse] 的 [Pool].
 */
internal class TemplateCursePool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<TemplateCurse, GenerationContext>>,
    override val filters: NodeContainer<Filter<GenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<TemplateCurse, GenerationContext>() {
    override fun whenSelect(value: TemplateCurse, context: GenerationContext) {
        context.curses += CurseContextHolder(value.key)
    }
}

/**
 * ## Node structure 1 (fallback)
 *
 * ```yaml
 * <node>:
 *   - key: curse:highest_damage
 *     weight: 1
 *     element: fire
 *     amount: 10
 *   - key: curse:highest_damage
 *     weight: 1
 *     element: water
 *     amount: 16
 * ```
 *
 * ## Node structure 2
 *
 * ```yaml
 * <node>:
 *   filters:
 *     - type: rarity
 *       rarity: common
 *   entries:
 *     - key: curse:highest_damage
 *       weight: 1
 *       element: fire
 *       amount: 20
 *     - key: curse:highest_damage
 *       weight: 1
 *       element: water
 *       amount: 32
 * ```
 */
internal object TemplateCursePoolSerializer : KoinComponent, PoolSerializer<TemplateCurse, GenerationContext>() {
    override val sampleNodeFacade: TemplateCurseSampleNodeFacade by inject()
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<TemplateCurse, GenerationContext>>,
        filters: NodeContainer<Filter<GenerationContext>>,
        isReplacement: Boolean,
    ): Pool<TemplateCurse, GenerationContext> {
        return TemplateCursePool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }

    override fun valueConstructor(node: ConfigurationNode): TemplateCurse {
        return node.krequire<TemplateCurse>()
    }

    override fun filterConstructor(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }
}

/**
 * @see GroupSerializer
 */
internal object TemplateCurseGroupSerializer : KoinComponent, GroupSerializer<TemplateCurse, GenerationContext>() {
    override val filterNodeFacade: ItemFilterNodeFacade by inject()

    override fun poolConstructor(node: ConfigurationNode): Pool<TemplateCurse, GenerationContext> {
        return node.krequire<Pool<TemplateCurse, GenerationContext>>()
    }

    override fun filterConstructor(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }
}
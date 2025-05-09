package cc.mewcraft.wakame.item.templates.components.cells

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.entity.attribute.bundle.element
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.components.cells.cores.AttributeCoreArchetype
import cc.mewcraft.wakame.item.templates.components.cells.cores.EmptyCoreArchetype
import cc.mewcraft.wakame.item.templates.components.cells.cores.VirtualCoreArchetype
import cc.mewcraft.wakame.item.templates.filters.AttributeFilter
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.random3.*
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * 代表一个 [核心][Core] 的模板.
 */
interface CoreArchetype {
    /**
     * 核心的唯一标识. 主要用于序列化实现.
     *
     * - 该对象的 [Key.namespace] 用来区分不同基本类型的核心
     *     - 例如: 属性, 技能...
     * - 该对象的 [Key.value] 用来区分同一类型下不同的实体
     *     - 例如对于属性: 攻击力属性, 防御力属性...
     *     - 例如对于技能: 火球术, 冰冻术...
     */
    val id: Key

    /**
     * 从该模板生成一个 [Core] 实例.
     *
     * @param context 物品生成的上下文
     * @return 生成的新核心
     */
    fun generate(context: ItemGenerationContext): Core
}

/**
 * [CoreArchetype] 的序列化器.
 */
internal object CoreArchetypeSerializer : TypeSerializer2<CoreArchetype> {
    override fun deserialize(type: Type, node: ConfigurationNode): CoreArchetype {
        val type1 = node.node("type").require<Key>()
        val core = when {
            type1 == GenericKeys.NOOP -> {
                VirtualCoreArchetype
            }

            type1 == GenericKeys.EMPTY -> {
                EmptyCoreArchetype
            }

            type1.namespace() == Namespaces.ATTRIBUTE -> {
                val attributeId = type1
                AttributeCoreArchetype(attributeId, node)
            }

            // 大概是配置文件写错了
            else -> {
                throw SerializationException(node, type, "Unknown namespaced key for template core: ${type1.namespace()}")
            }
        }

        return core
    }
}

/**
 * [CoreArchetype] 的 [Pool].
 */
internal class CoreArchetypePool(
    override val amount: Long,
    override val samples: NodeContainer<Sample<CoreArchetype, ItemGenerationContext>>,
    override val filters: NodeContainer<Filter<ItemGenerationContext>>,
    override val isReplacement: Boolean,
) : Pool<CoreArchetype, ItemGenerationContext>() {
    override fun whenSelect(value: CoreArchetype, context: ItemGenerationContext) {
        // context writes are delayed after the template is realized
    }
}

/**
 * ## Node structure 1 (fallback)
 *
 * ```yaml
 * <node>:
 *   - type: attribute:movement_speed
 *     weight: 1
 *     value: 0.4
 *   - type: attribute:defense
 *     weight: 1
 *     value: 23
 * ```
 *
 * ## Node structure 2
 *
 * ```yaml
 * <node>:
 *   filters:
 *     - type: item:rarity
 *       rarity: rare
 *   entries:
 *     - type: attribute:movement_speed
 *       weight: 1
 *       value: 0.4
 *     - type: attribute:defense
 *       weight: 1
 *       value: 23
 * ```
 */
internal object CoreArchetypePoolSerializer : PoolSerializer<CoreArchetype, ItemGenerationContext>() {
    override val sampleNodeFacade = CoreArchetypeSampleNodeFacade
    override val filterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<CoreArchetype, ItemGenerationContext>>,
        filters: NodeContainer<Filter<ItemGenerationContext>>,
        isReplacement: Boolean,
    ): Pool<CoreArchetype, ItemGenerationContext> {
        return CoreArchetypePool(
            amount = amount,
            samples = samples,
            filters = filters,
            isReplacement = isReplacement,
        )
    }
}

/**
 * [CoreArchetype] 的 [cc.mewcraft.wakame.random3.Group] 的序列化器.
 */
internal object CoreArchetypeGroupSerializer : GroupSerializer<CoreArchetype, ItemGenerationContext>() {
    override val filterNodeFacade = ItemFilterNodeFacade

    override fun poolConstructor(node: ConfigurationNode): Pool<CoreArchetype, ItemGenerationContext> {
        return node.require<Pool<CoreArchetype, ItemGenerationContext>>()
    }
}

/**
 * 封装了类型 [CoreArchetype] 所需要的所有 [Node] 相关的实现.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeFacadeRegistryLoader::class, // deps: 需要直接的数据, 必须指定依赖
    ],
)
@Reload
internal object CoreArchetypeSampleNodeFacade : SampleNodeFacade<CoreArchetype, ItemGenerationContext>() {
    override val dataDir: Path = Path("random/item_core")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder()
//        .registerAll(Injector.get(named(ABILITY_EXTERNALS)))
        .register<CoreArchetype>(CoreArchetypeSerializer)
        .register<Filter<ItemGenerationContext>>(FilterSerializer)
        .build()
    override val repository: NodeRepository<Sample<CoreArchetype, ItemGenerationContext>> = NodeRepository()
    override val sampleDataType: TypeToken<CoreArchetype> = typeTokenOf()
    override val filterNodeFacade: ItemFilterNodeFacade = ItemFilterNodeFacade

    @InitFun
    fun init() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    fun reload() {
        NodeFacadeSupport.reload(this)
    }

    override fun intrinsicFilters(value: CoreArchetype): Collection<Filter<ItemGenerationContext>> {
        val filter = when (value) {
            // A noop core should always return true
            is VirtualCoreArchetype -> {
                Filter.alwaysTrue()
            }

            // An empty core should always return true
            is EmptyCoreArchetype -> {
                Filter.alwaysTrue()
            }

            // By design, an attribute is considered generated
            // if there is already an attribute with all the same
            // key, operation and element in the selection context.
            is AttributeCoreArchetype -> {
                val attributeId = value.id.value()
                val attribute = value.data
                AttributeFilter(true, attributeId, attribute.operation, attribute.element)
            }

            // Throw if we see an unknown schema core type
            else -> {
                throw SerializationException("Can't create intrinsic conditions for unknown template core: ${value}. This is a bug.")
            }
        }
        return listOf(filter)
    }

    override fun decodeSampleData(node: ConfigurationNode): CoreArchetype {
        return node.require<CoreArchetype>()
    }

}
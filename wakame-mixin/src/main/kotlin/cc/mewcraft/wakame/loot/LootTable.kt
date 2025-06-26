package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item2.data.impl.Core
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.adventure.toSimpleString
import io.leangen.geantyref.TypeFactory
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.stream.Stream

inline fun <reified S> LootTable(
    pools: List<LootPool<S>>
): LootTable<S> = SimpleLootTable(pools)

/**
 * [LootTable] 是一个包含了若干 [LootPool] 的集合
 */
interface LootTable<S> {
    companion object {
        val SERIALIZER: TypeSerializer2<LootTable<*>> = Serializer

        fun <T> empty(): LootTable<T> {
            return SimpleLootTable(emptyList())
        }
    }

    /**
     * 这个 [LootTable] 中的所有 [LootPool].
     */
    val pools: List<LootPool<S>>

    /**
     * 选择 [LootTable] 中的样本.
     */
    fun select(context: LootContext): List<S>

    private object Serializer : TypeSerializer2<LootTable<*>> {
        override fun deserialize(type: Type, node: ConfigurationNode): LootTable<*> {
            val rawScalar = node.rawScalar()
            if (rawScalar is String) {
                // 如果 rawScalar 是字符串, 则是配置文件中指定了 LootTable 的名称, 需要从注册表中读取
                return BuiltInRegistries.LOOT_TABLE[rawScalar]
                    ?: throw SerializationException(node, type, "无法从配置文件中读取名为 $rawScalar 的 LootTable")
            }
            // 如果 rawScalar 不是字符串, 则是配置文件中直接指定了一整个 LootTable 对象, 或是 LootTable 系统本身在序列化, 进行对应逻辑.
            val type = type as ParameterizedType
            var sType = type.actualTypeArguments[0]
            if (sType is WildcardType) {
                // 如果 sType 是通配符类型, 则是配置文件中指定了类型, 需要从配置文件中读取
                val configType = node.node("type").get<String>()
                    ?: throw SerializationException(node, type, "无法从配置文件中读取类型, 请在配置文件中指定 type 字段, 或请在代码中明确类型")
                sType = when (configType) {
                    "core" -> {
                        // LootPool<Core>
                        Core::class.java
                    }

                    "element" -> {
                        // LootPool<RegistryEntry<Element>>
                        TypeFactory.parameterizedClass(RegistryEntry::class.java, Element::class.java)
                    }

                    else -> throw SerializationException(node, type, "Unknown type $sType")
                }
            }
            val poolType = TypeFactory.parameterizedClass(LootPool::class.java, sType) // LootPool<S>
            val poolTypeToken = TypeToken.get(poolType) as TypeToken<LootPool<Any>>
            val pools = node.node("pools").getList(poolTypeToken) ?: throw SerializationException(node, type, "Failed to deserialize LootTable: pools is null or empty")
            return SimpleLootTable(pools)
        }
    }
}

/* Implementations */

// 最基本的 LootTable 实现
@PublishedApi
internal class SimpleLootTable<S>(
    override val pools: List<LootPool<S>>,
) : LootTable<S>, Examinable {
    override fun select(context: LootContext): List<S> {
        val result = mutableListOf<S>()
        val correctPools = if (context.selectEverything) {
            // 如果忽略条件, 则直接使用所有的 pool
            pools
        } else {
            // 过滤掉不满足条件的 pool
            pools.filter { pool -> pool.conditions.all { it.invoke(context) } }
        }

        for (pool in correctPools) {
            // 我们找到了一个满足条件的 pool, 因此将这个 pool 选择的结果添加到结果列表中
            result += pool.select(context)
        }

        return result
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("pools", pools)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

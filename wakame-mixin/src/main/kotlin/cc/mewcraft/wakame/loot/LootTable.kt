package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.adventure.toSimpleString
import io.leangen.geantyref.TypeFactory
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
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
            val type = type as ParameterizedType
            val sType = type.actualTypeArguments[0]
            val poolType = TypeFactory.parameterizedClass(LootPool::class.java, sType) // LootPool<S>
            val poolTypeToken = TypeToken.get(poolType) as TypeToken<LootPool<Any>>
            val pools = node.node("pools").getList(poolTypeToken) ?:
                throw SerializationException(node, type, "Failed to deserialize LootTable: pools is null or empty")
            return SimpleLootTable(pools)
        }
    }
}

/* Implementations */

// 最基本的 LootTable 实现, 用于测试使用
@PublishedApi
internal open class SimpleLootTable<S>(
    final override val pools: List<LootPool<S>>,
) : LootTable<S>, Examinable {
    final override fun select(context: LootContext): List<S> {
        val result = mutableListOf<S>()
        val correctPools = pools.filter { pool ->
            // 过滤掉不满足条件的 pool
            pool.conditions.all { it.invoke(context) }
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

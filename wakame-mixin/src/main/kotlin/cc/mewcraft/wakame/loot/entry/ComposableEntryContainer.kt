package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.TypeFactory
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun interface ComposableEntryContainer<S> {

    companion object {
        val SERIALIZER: TypeSerializer2<ComposableEntryContainer<*>> = Serializer

        fun <S> alwaysTrue(): ComposableEntryContainer<S> {
            return ComposableEntryContainer { _: LootContext, _: (LootPoolEntry<S>) -> Unit -> true }
        }

        fun <S> alwaysFalse(): ComposableEntryContainer<S> {
            return ComposableEntryContainer { _: LootContext, _: (LootPoolEntry<S>) -> Unit -> false }
        }
    }

    /**
     * 扩展此 [ComposableEntryContainer] 中的条目,
     * 当实现可传递 [LootPoolEntry], 会将其传递给 [dataConsumer].
     *
     * @param context 上下文, 用于提供必要的条件或状态信息.
     * @param dataConsumer 用于处理每个条目的消费者.
     *
     * @return 如果执行成功, 则返回 `true`; 否则返回 `false`.
     */
    fun expand(context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit): Boolean

    /**
     * 将当前的 [ComposableEntryContainer] 与另一个条目容器进行逻辑与操作.
     *
     * @return 一个新的 [ComposableEntryContainer], 其扩展逻辑为当前容器和提供的容器的逻辑与.
     */
    fun and(entry: ComposableEntryContainer<S>): ComposableEntryContainer<S> {
        return ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
            this.expand(context, dataConsumer) && entry.expand(context, dataConsumer)
        }
    }

    /**
     * 将当前的 [ComposableEntryContainer] 与另一个条目容器进行逻辑或操作.
     *
     * @return 一个新的 [ComposableEntryContainer], 其扩展逻辑为当前容器和提供的容器的逻辑或.
     */
    fun or(entry: ComposableEntryContainer<S>): ComposableEntryContainer<S> {
        return ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
            this.expand(context, dataConsumer) || entry.expand(context, dataConsumer)
        }
    }

    private object Serializer : TypeSerializer2<ComposableEntryContainer<*>> {
        override fun deserialize(type: Type, node: ConfigurationNode): ComposableEntryContainer<*> {
            val id = node.node("type").require<String>()
            val dataType = BuiltInRegistries.LOOT_POOL_ENTRY_TYPE[id] ?: throw SerializationException(node, type, "Unknown loot pool entry type: $type")
            return getDataValue(dataType, type, node)
        }

        private fun getDataValue(
            dataType: LootPoolEntryType<*>,
            type: Type,
            node: ConfigurationNode,
        ): ComposableEntryContainer<*> {
            val sType = (type as ParameterizedType).actualTypeArguments[0]
            val rawSubclass = GenericTypeReflector.erase(dataType.typeToken.type) // out ComposableEntryContainer.class
            val actualType = TypeFactory.parameterizedClass(rawSubclass, sType) // out ComposableEntryContainer<S>
            val serializer = dataType.serializer

            return serializer.deserialize(actualType, node) ?: throw SerializationException(node, type, "Failed to deserialize loot pool entry of type: $dataType with type: $actualType")
        }
    }
}
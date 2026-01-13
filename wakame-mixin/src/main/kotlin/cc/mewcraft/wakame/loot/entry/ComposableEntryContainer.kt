package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.registry.BuiltInRegistries
import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.TypeFactory
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 可组合的战利品条目容器接口，用于扩展并逻辑组合多个战利品条目容器.
 *
 * 该接口提供了扩展战利品条目的能力，并支持逻辑“与”或“或”操作来组合多个条目容器.
 * 主要用于在战利品表中根据不同的条件组合和处理多个条目.
 *
 * @param S 战利品数据的类型.
 */
fun interface ComposableEntryContainer<S> {

    companion object {
        val SERIALIZER: SimpleSerializer<ComposableEntryContainer<*>> = Serializer

        /**
         * 返回一个始终返回 `true` 的 [ComposableEntryContainer] 实例。
         *
         * 该方法适用于需要无条件扩展的场景。
         */
        fun <S> alwaysTrue(): ComposableEntryContainer<S> {
            return ComposableEntryContainer { _: LootContext, _: (LootPoolEntry<S>) -> Unit -> true }
        }

        /**
         * 返回一个始终返回 `false` 的 [ComposableEntryContainer] 实例。
         *
         * 该方法适用于需要无条件不扩展的场景。
         */
        fun <S> alwaysFalse(): ComposableEntryContainer<S> {
            return ComposableEntryContainer { _: LootContext, _: (LootPoolEntry<S>) -> Unit -> false }
        }
    }

    /**
     * 扩展此 [ComposableEntryContainer] 中的条目,
     * 当实现可传递 [LootPoolEntry], 会将其传递给 [dataConsumer].
     *
     * @param context 上下文, 用于评估指定的条件, 获取状态信息.
     * @param dataConsumer 仅在条件满足时处理战利品条目数据的消费者函数.
     *
     * @return 如果战利品条目被扩展或处理, 则返回 `true`; 否则返回 `false`.
     */
    fun expand(context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit): Boolean

    /**
     * 将当前的 [ComposableEntryContainer] 与另一个条目容器进行逻辑与操作.
     *
     * @return 一个新的 [ComposableEntryContainer], 其扩展逻辑为当前容器和提供的容器的逻辑与.
     */
    fun and(entry: ComposableEntryContainer<S>): ComposableEntryContainer<S> {
        return ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
            if (context.selectEverything) {
                // 如果正在迭代, 则扩展所有条目
                this.expand(context, dataConsumer)
                entry.expand(context, dataConsumer)
            } else {
                this.expand(context, dataConsumer) && entry.expand(context, dataConsumer) // 如果不是迭代, 则只在满足条件时扩展条目
            }
        }
    }

    /**
     * 将当前的 [ComposableEntryContainer] 与另一个条目容器进行逻辑或操作.
     *
     * @return 一个新的 [ComposableEntryContainer], 其扩展逻辑为当前容器和提供的容器的逻辑或.
     */
    fun or(entry: ComposableEntryContainer<S>): ComposableEntryContainer<S> {
        return ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
            if (context.selectEverything) {
                // 如果正在迭代, 则扩展所有条目
                this.expand(context, dataConsumer)
                entry.expand(context, dataConsumer)
            } else {
                this.expand(context, dataConsumer) || entry.expand(context, dataConsumer) // 如果不是迭代, 则只在满足条件时扩展条目
            }
        }
    }

    private object Serializer : SimpleSerializer<ComposableEntryContainer<*>> {
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
package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.context.LootContext

fun interface ComposableEntryContainer<S> {
    companion object {
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
}
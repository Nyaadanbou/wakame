package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import io.leangen.geantyref.TypeFactory
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.ParameterizedType

/**
 * 战利品池复合条目的基础抽象类，用于组合多个子条目.
 *
 * 该类表示一个“复合型”战利品条目容器(如 Alternatives、Sequence 等)，它将多个子容器组合为一个统一的逻辑整体.
 * 子类需要实现具体的组合策略 (如并列, 顺序, 任选其一等).
 *
 * @param S 战利品数据的类型.
 * @param children 子条目列表，每个子条目本身也是一个 [LootPoolEntryContainer].
 * @param conditions 当前条目所需满足的条件列表.
 */
abstract class CompositeEntryBase<S>(
    protected val children: List<LootPoolEntryContainer<S>>,
    conditions: List<LootPredicate>,
) : LootPoolEntryContainer<S>(conditions) {

    companion object {
        protected fun <T : CompositeEntryBase<*>> makeSerializer(factory: (List<LootPoolEntryContainer<Any>>, List<LootPredicate>) -> T): TypeSerializer2<T> {
            return TypeSerializer2 { type, node ->
                val sType = (type as ParameterizedType).actualTypeArguments[0]
                val entryType = TypeFactory.parameterizedClass(LootPoolEntryContainer::class.java, sType) // LootPoolEntryContainer<S>
                val childListType = TypeFactory.parameterizedClass(List::class.java, entryType) // List<LootPoolEntryContainer<S>>
                val children = node.node("children").get(TypeToken.get(childListType)) as List<LootPoolEntryContainer<Any>>
                val conditions = node.node("conditions").get<List<LootPredicate>>() ?: emptyList()
                factory.invoke(children, conditions)
            }
        }
    }

    private val composedChildren: ComposableEntryContainer<S> = compose(children)

    /**
     * 子类需要实现的组合策略，将多个 [ComposableEntryContainer] 组合为一个逻辑容器。
     *
     * 例如：
     * - 随机选取一个(Alternatives)
     * - 顺序执行(Sequence)
     * - 所有满足条件的都尝试执行(Group)
     *
     * @param children 子条目容器列表
     * @return 组合后的逻辑容器
     */
    protected abstract fun compose(children: List<ComposableEntryContainer<S>>): ComposableEntryContainer<S>

    final override fun expand(context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit): Boolean {
        return this.canRun(context) && this.composedChildren.expand(context, dataConsumer)
    }
}
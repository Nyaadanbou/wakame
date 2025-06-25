package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import io.leangen.geantyref.TypeFactory
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.ParameterizedType

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

    protected abstract fun compose(children: List<ComposableEntryContainer<S>>): ComposableEntryContainer<S>

    final override fun expand(context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit): Boolean {
        return this.canRun(context) && this.composedChildren.expand(context, dataConsumer)
    }
}
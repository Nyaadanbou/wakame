package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition
import me.lucko.helper.random.Weigher

@PublishedApi
internal abstract class AbstractSample<T, C : SelectionContext>(
    override val content: T,
    override val weight: Double,
    override val conditions: List<Condition<C>>,
    override val mark: Mark<*>?,
) : Sample<T, C>

@PublishedApi
internal class SampleBuilderImpl<T, C : SelectionContext>(
    override val content: T,
) : SampleBuilder<T, C> {
    override var weight: Double = 1.0
    override var conditions: MutableList<Condition<C>> = ArrayList()
    override var mark: Mark<*>? = null
    override var trace: (C) -> Unit = {}
}

internal object SampleWeigher : Weigher<Sample<*, *>> {
    override fun weigh(element: Sample<*, *>): Double {
        return element.weight
    }
}

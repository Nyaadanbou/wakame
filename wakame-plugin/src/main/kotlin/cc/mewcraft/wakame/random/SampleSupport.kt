package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition

@PublishedApi
internal abstract class AbstractSample<T, C : SelectionContext>(
    override val content: T,
    override val weight0: Double,
    override val conditions: List<Condition<C>>,
    override val mark: Mark<*>?,
) : Sample<T, C>

@PublishedApi
internal class SampleBuilder<T, C : SelectionContext>(
    override val content: T,
) : Sample.Builder<T, C> {
    override var weight: Double = 1.0
    override var conditions: MutableList<Condition<C>> = ArrayList()
    override var mark: Mark<*>? = null
    override var trace: (C) -> Unit = {}
}

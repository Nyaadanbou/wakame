package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition

@PublishedApi
internal abstract class AbstractSample<out T, C>(
    override val content: T,
    override val weight0: Double,
    override val conditions: List<Condition<C>>,
    override val metadata: SampleMeta<String>,
) : Sample<T, C>

@PublishedApi
internal class SampleBuilder<T, C>(
    override val content: T,
) : Sample.Builder<T, C> {
    override var weight: Double = 1.0
    override var conditions: MutableList<Condition<C>> = ArrayList()
    override var metadata: String? = null
    override var applyContext: (C) -> Unit = {}
}

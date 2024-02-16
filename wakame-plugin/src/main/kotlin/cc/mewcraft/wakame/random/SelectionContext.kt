package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.util.WatchedSet
import it.unimi.dsi.fastutil.objects.ObjectArraySet

/**
 * Represents a context that is both readable and writable by the whole
 * process of sample selection.
 *
 * @see BasicSelectionContext
 */
sealed interface SelectionContext {
    /**
     * All the [marks][Mark] that has been added to `this` context.
     */
    val marks: MutableSet<Mark<*>>
}

/**
 * You can (and should) extend this class to create your own context.
 */
open class BasicSelectionContext : SelectionContext {
    override val marks: MutableSet<Mark<*>> by WatchedSet(ObjectArraySet(8))
}

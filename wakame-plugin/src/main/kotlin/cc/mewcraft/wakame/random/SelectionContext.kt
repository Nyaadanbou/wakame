package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.util.WatchedSet
import kotlin.random.Random

/**
 * Represents a context that is both readable and writable by the whole
 * process of sample selection.
 *
 * @see BasicSelectionContext
 */
sealed interface SelectionContext {
    /**
     * The random generator for the selection process.
     */
    val random: Random

    /**
     * All the [marks][Mark] that has been added to `this` context.
     */
    val marks: MutableSet<Mark<*>>
}

/**
 * You can (and should) extend this class to create your own context.
 */
open class BasicSelectionContext(
    /**
     * The seed used to create the random generator for the selection process.
     *
     * It is guaranteed that the selection result is the same for the same seed.
     */
    seed: Long,
) : SelectionContext {
    override val random: Random = Random(seed)
    override val marks: MutableSet<Mark<*>> by WatchedSet(HashSet())
}

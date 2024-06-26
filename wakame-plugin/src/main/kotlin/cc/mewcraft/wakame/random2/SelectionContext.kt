package cc.mewcraft.wakame.random2

import cc.mewcraft.wakame.util.WatchedSet
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import kotlin.random.Random

/**
 * Represents a context that is both readable and writable by the whole
 * process of sample selection.
 *
 * You can extend this class to create your own context.
 */
open class SelectionContext(
    /**
     * The seed used to create the random generator for the selection process.
     *
     * It is guaranteed that the selection result is the same for the same seed.
     */
    val seed: Long,
) : Examinable {
    /**
     * The random generator to be used in the whole selection process.
     */
    val random: Random = Random(seed)

    /**
     * All the [marks][Mark] that has been added to `this` context.
     */
    val marks: MutableSet<Mark<*>> by WatchedSet(HashSet())

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("seed", seed),
            ExaminableProperty.of("marks", marks),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

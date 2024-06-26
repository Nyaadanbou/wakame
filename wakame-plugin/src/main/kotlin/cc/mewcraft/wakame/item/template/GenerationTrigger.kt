package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.user.User
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * A thing that triggers the neko stack generation.
 *
 * To be used with [GenerationContext].
 */
interface GenerationTrigger : Examinable {
    /**
     * Level of the source.
     */
    val level: Int

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("level", level)
        )
    }

    companion object {
        private val supportedSources: Set<KClass<*>> = setOf(
            User::class,
            Crate::class,
        )

        /**
         * Creates a fake trigger with specific [level].
         *
         * @param level a constant level
         * @return the fake source
         */
        fun fake(level: Int): GenerationTrigger {
            return FakeGenerationTrigger(level)
        }

        /**
         * Wraps the source as [GenerationTrigger].
         *
         * The source must be one of the following type:
         * - [User]
         * - [Crate]
         *
         * @param source the source that triggers the generation
         * @return the wrapped source
         */
        fun wrap(source: Any): GenerationTrigger {
            require(supportedSources.any { it.isInstance(source) }) { "Unsupported trigger source: ${source::class.qualifiedName}" }
            return RealGenerationTrigger(source)
        }
    }
}

private class FakeGenerationTrigger(
    override val level: Int,
) : GenerationTrigger

private class RealGenerationTrigger(
    private val source: Any,
) : GenerationTrigger {
    override val level: Int
        get() = when (source) {
            is User<*> -> source.level
            is Crate -> source.level
            else -> error("Unknown level getter")
        }
}
package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.user.User
import kotlin.reflect.KClass

/**
 * A thing that triggers the neko stack generation.
 *
 * To be used with [SchemaGenerationContext].
 */
interface GenerationTrigger {
    /**
     * Level of the source.
     */
    val level: Int

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
         * Wraps the source as [SchemaGenerationTrigger].
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
            return RealSchemaGenerationTrigger(source)
        }
    }
}

private class FakeGenerationTrigger(
    override val level: Int,
) : GenerationTrigger

private class RealSchemaGenerationTrigger(
    private val source: Any,
) : GenerationTrigger {
    override val level: Int
        get() = when (source) {
            is User<*> -> source.level
            is Crate -> source.level
            else -> error("Unknown level getter")
        }
}
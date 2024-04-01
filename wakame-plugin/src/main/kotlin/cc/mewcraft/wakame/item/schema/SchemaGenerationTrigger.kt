package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.user.User
import kotlin.reflect.KClass

/**
 * A thing that triggers the neko stack generation.
 *
 * To be used with [SchemaGenerationContext].
 */
interface SchemaGenerationTrigger {
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
        fun fake(level: Int): SchemaGenerationTrigger {
            return FakeSchemaGenerationTrigger(level)
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
        fun wrap(source: Any): SchemaGenerationTrigger {
            require(supportedSources.any { it.isInstance(source) }) { "Unsupported trigger source: ${source::class.qualifiedName}" }
            return RealSchemaGenerationTrigger(source)
        }
    }
}

private class FakeSchemaGenerationTrigger(
    override val level: Int,
) : SchemaGenerationTrigger

private class RealSchemaGenerationTrigger(
    private val source: Any,
) : SchemaGenerationTrigger {
    override val level: Int
        get() = when (source) {
            is User<*> -> source.level
            is Crate -> source.level
            else -> error("Unknown level getter")
        }
}
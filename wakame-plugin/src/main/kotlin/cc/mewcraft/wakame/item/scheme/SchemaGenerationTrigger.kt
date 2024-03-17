package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.user.User
import kotlin.reflect.KClass

/**
 * A thing that triggers the neko stack generation.
 *
 * To be used with [SchemeGenerationContext].
 */
class SchemaGenerationTrigger private constructor(
    private val source: Any,
) {
    companion object {
        private val supportedSources: Set<KClass<*>> = setOf(
            User::class,
            Crate::class,
        )

        /**
         * Wraps the source as [SchemaGenerationTrigger].
         *
         * @param source the source that triggers the generation
         * @return the wrapped source
         */
        fun wrap(source: Any): SchemaGenerationTrigger {
            require(supportedSources.any { it.isInstance(source) }) { "Unsupported trigger source: ${source::class.qualifiedName}" }
            return SchemaGenerationTrigger(source)
        }
    }

    /**
     * Level of the source.
     */
    val level: Int
        get() = when (source) {
            is User -> source.level
            is Crate -> source.level
            else -> error("Unknown level getter")
        }
}
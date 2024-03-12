package cc.mewcraft.wakame.player

import com.google.common.collect.ImmutableMap
import kotlin.reflect.KClass
import org.bukkit.entity.Player as PaperPlayer


/**
 * Used to adapt a platform [P] into a wakame [Player].
 *
 * @param P the platform player type
 */
interface PlayerAdapter<P> {
    fun adapt(player: P): Player
}

/**
 * The registry of all available [adapters][PlayerAdapter].
 *
 * Use it to get a platform player adapter.
 */
object PlayerAdapters {
    private val registry: Map<KClass<*>, PlayerAdapter<*>> = ImmutableMap.builder<KClass<*>, PlayerAdapter<*>>().apply {
        put(PaperPlayer::class, PaperPlayerAdapter)
    }.build()

    fun <P> get(clazz: KClass<*>): PlayerAdapter<P> {
        @Suppress("UNCHECKED_CAST")
        return (registry[clazz] ?: error("Unsupported player type: ${clazz.qualifiedName}")) as PlayerAdapter<P>
    }

    inline fun <reified P> get(): PlayerAdapter<P> {
        return get(P::class)
    }
}

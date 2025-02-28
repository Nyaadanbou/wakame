package cc.mewcraft.wakame.user

import org.bukkit.entity.Player
import java.util.UUID
import kotlin.reflect.KClass
import org.bukkit.entity.Player as PaperPlayer


/**
 * Used to adapt a platform [P] into a wakame [User].
 *
 * @param P the platform player type
 */
interface PlayerAdapter<P> {
    fun adapt(player: P): User<P>
    fun adapt(uniqueId: UUID): User<P>
}

/**
 * Holder of the [PlayerAdapter].
 *
 * Use it to get your platform player adapter.
 */
object PlayerAdapters {
    // a naive implementation for the selection of player class and adapter
    private val playerClazz: KClass<*> = PaperPlayer::class
    private val playerAdapter: PlayerAdapter<*> = PaperPlayerAdapter

    fun <P> get(clazz: KClass<*>): PlayerAdapter<P> {
        require(this.playerClazz == clazz) { "Player class " + clazz.simpleName + " is not assignable from " + this.playerClazz.simpleName }
        return (@Suppress("UNCHECKED_CAST") (playerAdapter as PlayerAdapter<P>))
    }

    inline fun <reified P> get(): PlayerAdapter<P> {
        return get(P::class)
    }
}


/**
 * The adapter for [org.bukkit.entity.Player][Player].
 */
object PaperPlayerAdapter : PlayerAdapter<Player> {

    override fun adapt(player: Player): User<Player> {
        return PaperUserManager.getUser(player)
    }

    override fun adapt(uniqueId: UUID): User<Player> {
        return PaperUserManager.getUser(uniqueId)
    }

}

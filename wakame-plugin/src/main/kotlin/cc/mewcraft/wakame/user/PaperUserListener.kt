package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * The User Manager on Paper platform.
 */
class PaperUserListener : KoinComponent, UserListener, UserManager<Player> {
    private val server: Server by inject()

    // holds the live data of users
    private val userRepository: Cache<Player, User<Player>> = Caffeine.newBuilder()
        .weakKeys()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build()

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        // cleanup user data for the player
        val user = getPlayer(player)
        player.persistentDataContainer.set(UserListener.PLAYER_HEALTH, PersistentDataType.DOUBLE, player.health)
        player.persistentDataContainer.set(UserListener.PLAYER_MANA, PersistentDataType.INTEGER, user.resourceMap.current(ResourceTypeRegistry.MANA))

        user.skillMap.clear()
        userRepository.invalidate(player)
    }

    @EventHandler
    private fun onJoin(e: PlayerJoinEvent) {
        // create user data for the player
        val user = getPlayer(e.player)

        val health = e.player.persistentDataContainer.get(UserListener.PLAYER_HEALTH, PersistentDataType.DOUBLE)
        if (health != null) {
            e.player.health = health
        }
        val mana = e.player.persistentDataContainer.get(UserListener.PLAYER_MANA, PersistentDataType.INTEGER)
        if (mana != null) {
            user.resourceMap.set(ResourceTypeRegistry.MANA, mana)
        }
    }

    override fun getPlayer(uniqueId: UUID): User<Player> {
        val player = requireNotNull(server.getPlayer(uniqueId)) { "Player '$uniqueId' is not online" }
        return getPlayer(player)
    }

    override fun getPlayer(player: Player): User<Player> {
        return userRepository.get(player) { k -> PaperUser(k) }
    }
}
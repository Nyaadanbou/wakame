package cc.mewcraft.wakame.user

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import java.util.concurrent.TimeUnit

class PaperUserManager : Listener, KoinComponent, UserManager<Player> {
    private val server: Server by inject()

    // holds the live data of users
    private val userRepository: Cache<Player, User<Player>> = Caffeine.newBuilder()
        .weakKeys()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .removalListener<Player, User<Player>> { _, value, _ ->
            value?.cleanup()
        }
        .build()

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        val player = e.player

        // clean up user data
        unloadUser(player)
    }

    @EventHandler
    private fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        // load user data
        getUser(player)
    }

    private fun unloadUser(player: Player) {
        // clean up user data
        userRepository.invalidate(player)
    }

    override fun getUser(uniqueId: UUID): User<Player> {
        return getUser(requireNotNull(server.getPlayer(uniqueId)) { "Player '$uniqueId' is not online" })
    }

    override fun getUser(player: Player): User<Player> {
        return userRepository.get(player) { k -> PaperUser(k) }
    }
}
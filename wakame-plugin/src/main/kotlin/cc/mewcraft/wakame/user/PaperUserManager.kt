package cc.mewcraft.wakame.user

import com.github.benmanes.caffeine.cache.*
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class PaperUserManager : Listener, KoinComponent, UserManager<Player> {
    private val server: Server by inject()

    // holds the live data of users
    private val userRepository: Cache<Player, User<Player>> = Caffeine.newBuilder()
        .weakKeys()
        .removalListener<Player, User<Player>> { _, value, cause: RemovalCause ->
            if (cause.wasEvicted()) {
                value!!.cleanup()
            }
        }
        .build()

    @EventHandler(
        // 尽可能早的创建 User 对象, 以便其他系统使用
        priority = EventPriority.LOWEST
    )
    private fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        // load or create user data
        loadUser(player)

        // set health scale to 20.0
        player.healthScale = 20.0
    }

    @EventHandler(
        // 尽可能晚的移除 User 对象, 以便其他系统使用
        priority = EventPriority.MONITOR
    )
    private fun onQuit(e: PlayerQuitEvent) {
        val player = e.player

        // clean up user data
        unloadUser(player)
    }

    private fun loadUser(player: Player): User<Player> {
        // cache user data
        return userRepository.get(player) { k -> PaperUser(k) }
    }

    private fun unloadUser(player: Player) {
        // clean up user data
        userRepository.invalidate(player)
    }

    override fun getUser(uniqueId: UUID): User<Player> {
        return getUser(requireNotNull(server.getPlayer(uniqueId)) { "player '$uniqueId' is not online" })
    }

    override fun getUser(player: Player): User<Player> {
        return loadUser(player)
    }
}
package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.PlayerAttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.kizami.PlayerKizamiMap
import cc.mewcraft.wakame.level.PlayerLevelProvider
import cc.mewcraft.wakame.resource.PlayerResourceMap
import cc.mewcraft.wakame.resource.ResourceMap
import cc.mewcraft.wakame.skill.PlayerSkillMap
import cc.mewcraft.wakame.skill.SkillMap
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

/**
 * A wakame player in Paper platform.
 *
 * @property player the [paper player][Player]
 */
class PaperUser(
    override val player: Player,
) : User<Player>, KoinComponent {
    override val uniqueId: UUID
        get() = player.uniqueId
    override val level: Int
        get() = levelProvider.getOrDefault(uniqueId, 1)
    override val kizamiMap: KizamiMap = PlayerKizamiMap(this)
    override val attributeMap: AttributeMap = PlayerAttributeMap(this)
    override val skillMap: SkillMap = PlayerSkillMap(this)
    override val resourceMap: ResourceMap = PlayerResourceMap(this)

    private val levelProvider: PlayerLevelProvider by inject()
}

/**
 * Adapts the [Player] into [NekoPlayer][User].
 */
fun Player.toUser(): User<Player> {
    return PlayerAdapters.get<Player>().adapt(this)
}

/**
 * The adapter for [Player].
 */
class PaperPlayerAdapter : KoinComponent, Listener, PlayerAdapter<Player> {
    private val userManager: UserManager<Player> by inject()

    override fun adapt(player: Player): User<Player> {
        return userManager.getPlayer(player)
    }
}

/**
 * The User Manager on Paper platform.
 */
class PaperUserManager : KoinComponent, Listener, UserManager<Player> {
    private val server: Server by inject()

    // holds the live data of users
    private val userRepository: Cache<UUID, User<Player>> = Caffeine.newBuilder().build()

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        // cleanup user data for the player
        userRepository.invalidate(e.player.uniqueId)
    }

    @EventHandler
    private fun onJoin(e: PlayerJoinEvent) {
        // create user data for the player
        getPlayer(e.player)
    }

    override fun getPlayer(uniqueId: UUID): User<Player> {
        val player = requireNotNull(server.getPlayer(uniqueId)) { "Player '$uniqueId' is not online" }
        return getPlayer(player)
    }

    override fun getPlayer(player: Player): User<Player> {
        return userRepository.get(player.uniqueId) { _ -> PaperUser(player) }
    }
}
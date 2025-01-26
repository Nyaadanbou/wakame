package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.event
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

@Init(
    stage = InitStage.POST_WORLD,
)
internal object PaperUserManager : UserManager<Player> {
    // holds the live data of users
    private val userRepository: Cache<Player, User<Player>> = Caffeine.newBuilder()
        .removalListener<Player, User<Player>> { _, value, cause: RemovalCause ->
            if (cause.wasEvicted()) {
                value!!.cleanup()
            }
        }
        .build()

    @InitFun
    fun init() {

        // 监听 PlayerJoinEvent, 初始化 User 对象
        event<PlayerJoinEvent>(
            priority = EventPriority.LOWEST, // 尽可能早的创建 User 对象, 以便其他系统使用
        ) { event ->
            val player = event.player

            // 初始化 User 对象
            loadUser(player)

            // Koish 系统下玩家的最大生命值可以超过 20f,
            // 设置 healthScale 为 20f 避免红星占用过多屏幕
            // 但这也要求需要在其他地方显示玩家的当前/最大生命值
            player.healthScale = 20.0
        }

        // 监听 PlayerQuitEvent, 移除 User 对象
        event<PlayerQuitEvent>(
            priority = EventPriority.MONITOR, // 尽可能晚的移除 User 对象, 以便其他系统使用
        ) { event ->
            val player = event.player

            // 移除 User 对象
            unloadUser(player)
        }
    }

    private fun loadUser(player: Player): User<Player> {
        return userRepository.get(player, ::PaperUser)
    }

    private fun unloadUser(player: Player) {
        userRepository.invalidate(player)
    }

    override fun getUser(uniqueId: UUID): User<Player> {
        return getUser(requireNotNull(SERVER.getPlayer(uniqueId)) { "player '$uniqueId' is not online" })
    }

    override fun getUser(player: Player): User<Player> {
        return loadUser(player)
    }
}
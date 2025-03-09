package cc.mewcraft.wakame.integration.permission

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.HooksLoader
import cc.mewcraft.wakame.lifecycle.LifecycleDispatcher
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.*
import java.util.concurrent.CompletableFuture

// private data class PermissionArgs(val world: World, val player: OfflinePlayer, val permission: String)

/**
 * 通过已注册的 [PermissionIntegration] 来检查玩家的权限.
 *
 * 本实现不会对权限的检查结果缓存, 缓存由底层权限系统自己实现.
 * 如果服务器用的是 LuckPerms, LP 会缓存所有权限查询, 可以不必担心缓存问题.
 * 但如果服务器用的是其他权限插件, 请确保它们缓存了离线玩家的权限查询结果.
 * 否则每当查询离线玩家的权限时, 可能会导致性能问题.
 */
@Init(
    stage = InitStage.POST_WORLD,
    dispatcher = LifecycleDispatcher.ASYNC,
    runAfter = [HooksLoader::class]
)
object PermissionManager {

    internal val integrations = ArrayList<PermissionIntegration>()

    @InitFun
    fun init() {
        if (integrations.size > 1) {
            LOGGER.warn("Multiple permission integrations have been registered: ${integrations.joinToString { it::class.simpleName!! }}, Nekoo will use the first one")
        }
    }

    /**
     * 检查给定的 [玩家][player] 在给定的 [世界][world] 是否有给定的 [权限][permission].
     *
     * 该函数会使用 [org.bukkit.entity.Player.hasPermission] 来检查在线玩家的权限,
     * 否则会异步访问已注册的 [PermissionIntegrations][PermissionIntegration].
     */
    fun hasPermission(world: World, player: UUID, permission: String): CompletableFuture<Boolean> =
        hasPermission(world, Bukkit.getOfflinePlayer(player), permission)

    /**
     * 检查给定的 [玩家][player] 在给定的 [世界][world] 是否有给定的 [权限][permission].
     *
     * 该函数会使用 [org.bukkit.entity.Player.hasPermission] 来检查在线玩家的权限,
     * 否则会异步访问已注册的 [PermissionIntegrations][PermissionIntegration].
     */
    fun hasPermission(world: World, player: OfflinePlayer, permission: String): CompletableFuture<Boolean> {
        // 如果玩家在线, 直接使用 Bukkit 权限检查.
        // 理论上它会自动调用底层权限系统最快的缓存.
        val onlinePlayer = player.player
        if (onlinePlayer != null) {
            return CompletableFuture.completedFuture(onlinePlayer.hasPermission(permission))
        }

        // 如果玩家不在线, 那么就尝试离线查询权限.
        // 这一步我们自己不会缓存离线查询的结果, 而是交给底层权限系统自己实现.
        // 如果底层权限系统没有对离线玩家权限查询做缓存, 那么这里会有性能问题.
        // 如果服务器用的是 LuckPerms, 那么这里就不会有问题.
        //
        // 小声bb: 应该没有服务器会用其他权限插件吧?

        return hasPermissionWhenOffline(world, player, permission)
    }

    private fun hasPermissionWhenOffline(world: World, player: OfflinePlayer, permission: String): CompletableFuture<Boolean> {
        require(!Bukkit.isPrimaryThread()) {
            "offline player permissions should never be checked from the main thread"
        }
        return integrations[0].hasPermission(world, player, permission).thenApply { it == true }
    }

}
// 开发日记 2024/11/24
// Nova 在自己的 PermissionManager 当中给离线玩家写了个缓存,
// 这其实是做了最坏的假设: 它认为所有权限插件都没有对离线玩家缓存.
// 然而这一点对 LuckPerms 来说是不成立的 - LuckPerms 会对离线玩家的权限做缓存.

package cc.mewcraft.wakame.integration.permission

import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.concurrent.CompletableFuture

interface PermissionIntegration {

    fun hasPermission(world: World, player: OfflinePlayer, permission: String): CompletableFuture<Boolean?>

}
package cc.mewcraft.wakame.hook.impl.vault

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.permission.PermissionIntegration
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.concurrent.CompletableFuture

@Hook(plugins = ["Vault"], unless = ["LuckPerms"])
object VaultHook : PermissionIntegration {

    private val PERMISSIONS: Permission = Bukkit.getServicesManager().getRegistration(Permission::class.java)?.provider
        ?: error("service ${Permission::class.simpleName} not found")

    override fun hasPermission(world: World, player: OfflinePlayer, permission: String): CompletableFuture<Boolean?> {
        return CompletableFuture.completedFuture(PERMISSIONS.playerHas(world.name, player, permission))
    }

}
package cc.mewcraft.wakame.hook.impl.luckperms

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.permission.PermissionIntegration
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.query.QueryOptions
import net.luckperms.api.util.Tristate
import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.concurrent.CompletableFuture

@Hook(plugins = ["LuckPerms"])
object LuckPermsHook : PermissionIntegration {
    private val LUCK_PERMS = LuckPermsProvider.get()
    private val USER_MANAGER = LUCK_PERMS.userManager

    override fun hasPermission(world: World, player: OfflinePlayer, permission: String): CompletableFuture<Boolean?> {
        return USER_MANAGER.loadUser(player.uniqueId).thenApplyAsync { user ->
            user.cachedData
                .getPermissionData(QueryOptions.contextual(ImmutableContextSet.of("world", world.name)))
                .checkPermission(permission)
                .asNullableBoolean()
        }
    }
}

internal fun Tristate.asNullableBoolean() =
    when (this) {
        Tristate.TRUE -> true
        Tristate.FALSE -> false
        Tristate.UNDEFINED -> null
    }
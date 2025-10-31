package cc.mewcraft.wakame.hook.impl.husksync

import cc.mewcraft.wakame.entity.player.PlayerDataLoadingCoordinator
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.event
import net.william278.husksync.event.BukkitSyncCompleteEvent
import net.william278.husksync.user.BukkitUser

@Hook(plugins = ["HuskSync"])
object HuskSyncHook {

    init {
        PlayerDataLoadingCoordinator.registerExternalStage1Handler("HuskSync")

        event<BukkitSyncCompleteEvent> { event ->
            val user = (event.user as? BukkitUser)
            val player = user?.player ?: return@event
            PlayerDataLoadingCoordinator.getOrCreateSession(player).completeStage1()
        }
    }
}
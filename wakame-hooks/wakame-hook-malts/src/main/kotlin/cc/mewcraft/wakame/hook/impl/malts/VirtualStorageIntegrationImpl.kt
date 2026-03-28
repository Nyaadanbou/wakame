package cc.mewcraft.wakame.hook.impl.malts

import cc.mewcraft.wakame.integration.virtualstorage.VirtualStorageIntegration
import dev.jsinco.malts.api.MaltsAPI
import org.bukkit.entity.Player

class VirtualStorageIntegrationImpl : VirtualStorageIntegration {

    override fun openVault(player: Player, id: Int) {
        MaltsAPI.getVault(player.uniqueId, id).thenAccept { vault ->
            vault.open(player)
        }
    }
}
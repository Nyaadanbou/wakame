package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.pack.ResourcePackFacade.service
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.runTask
import cc.mewcraft.wakame.util.withDefaults
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent

object ResourcePackFacade : Initializable, KoinComponent {
    private const val RESOURCEPACK_CONFIG_NAME = "resourcepack.yml"

    val config = Configs.YAML.build(RESOURCEPACK_CONFIG_NAME) {
        withDefaults()
        defaultOptions { option ->
            option.serializers {
                it.kregister(ResourcePackServiceSerializer)
                it.kregister(ResourcePackPublisherSerializer)
            }
        }
    }

    val service: ResourcePackService by config.entry("service")
    val publisher: ResourcePackPublisher by config.entry("publisher")

    //<editor-fold desc="Resource Pack Generation">
    val description: String by config.entry("generation", "description")
    //</editor-fold>

    private fun initialize() {
        service.start()
    }

    override fun onPostWorld() {
        runTask { initialize() }
    }
}

internal class ResourcePackFacadeListener : Listener {
    @EventHandler
    private fun onCommandReload(e: NekoCommandReloadEvent) {
        service.stop()
        service.start()
    }
}
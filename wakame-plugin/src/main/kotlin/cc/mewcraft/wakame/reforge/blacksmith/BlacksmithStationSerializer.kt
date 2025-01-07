package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.Reforge
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry
import cc.mewcraft.wakame.serialization.configurate.mapperfactory.ObjectMappers
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

internal object BlacksmithStationSerializer {
    private const val ROOT_DIR_NAME = "blacksmith"
    private val LOGGER: Logger = Injector.get()

    fun loadAllStations(): Map<String, BlacksmithStation> {
        val blacksmithDirectory = Injector.get<File>(named(PLUGIN_DATA_DIR))
            .resolve(Reforge.ROOT_DIR_NAME)
            .resolve(ROOT_DIR_NAME)

        val yamlLoader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                registerAnnotatedObjects(ObjectMappers.DEFAULT)
            }
        }

        val result = blacksmithDirectory.walk()
            .maxDepth(1)
            .drop(1)
            .filter { it.isFile && it.extension == "yml" }
            .mapNotNull { f ->
                val id = f.nameWithoutExtension.lowercase()

                val rootNode = yamlLoader.buildAndLoadString(f.readText())
                val repairId = rootNode.node("repair").krequire<String>()
                val recycleId = rootNode.node("recycle").krequire<String>()
                val primaryMenuSettings = rootNode.node("primary_menu_settings").krequire<BasicMenuSettings>()
                val recyclingMenuSettings = rootNode.node("recycling_menu_settings").krequire<BasicMenuSettings>()
                val repairingMenuSettings = rootNode.node("repairing_menu_settings").krequire<BasicMenuSettings>()

                val repairingTable = RepairingTableRegistry.getTable(repairId) ?: run {
                    LOGGER.warn("Unknown repairing table: $repairId")
                    return@mapNotNull null
                }
                val recyclingStation = RecyclingStationRegistry.getStation(recycleId) ?: run {
                    LOGGER.warn("Unknown recycling station: $recycleId")
                    return@mapNotNull null
                }

                LOGGER.info("Loaded blacksmith station: $id")

                id to SimpleBlacksmithStation(
                    primaryMenuSettings, recyclingMenuSettings, repairingMenuSettings,
                    recyclingStation, repairingTable
                )
            }.toMap()

        return result
    }
}
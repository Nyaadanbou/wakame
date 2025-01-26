package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.require
import java.io.File

internal object BlacksmithStationSerializer {
    private const val DATA_DIR = "blacksmith"

    fun loadAllStations(): Map<String, BlacksmithStation> {
        val blacksmithDirectory = Injector.get<File>(InjectionQualifier.CONFIGS_FOLDER)
            .resolve(ReforgingStationConstants.DATA_DIR)
            .resolve(DATA_DIR)

        val yamlLoader = buildYamlConfigLoader {
            withDefaults()
        }

        val result = blacksmithDirectory.walk()
            .maxDepth(1)
            .drop(1)
            .filter { it.isFile && it.extension == "yml" }
            .mapNotNull { f ->
                val id = f.nameWithoutExtension.lowercase()

                val rootNode = yamlLoader.buildAndLoadString(f.readText())
                val repairId = rootNode.node("repair").require<String>()
                val recycleId = rootNode.node("recycle").require<String>()
                val primaryMenuSettings = rootNode.node("primary_menu_settings").require<BasicMenuSettings>()
                val recyclingMenuSettings = rootNode.node("recycling_menu_settings").require<BasicMenuSettings>()
                val repairingMenuSettings = rootNode.node("repairing_menu_settings").require<BasicMenuSettings>()

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
package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.eventbus.subscribe
import cc.mewcraft.wakame.gui.MenuLayoutSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.station.recipe.StationRecipeLoadEvent
import cc.mewcraft.wakame.util.*
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.*
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

internal object StationRegistry : Initializable, KoinComponent {
    private const val STATION_DIR_NAME = "station/stations"
    private val stations: MutableMap<String, Station> = mutableMapOf()
    private val logger: Logger by inject()

    /**
     * 获取所有合成站的唯一标识.
     */
    val NAMES: Set<String>
        get() = stations.keys

    fun find(id: String): Station? {
        return stations[id]
    }

    @VisibleForTesting
    fun loadStations() {
        stations.clear()

        val stationDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(STATION_DIR_NAME)
        stationDir.walk()
            .drop(1)
            .filter { it.extension == "yml" }
            .forEach { file ->
                try {
                    val fileText = file.readText()
                    val stationId = file.nameWithoutExtension
                    val stationNode = yamlConfig {
                        withDefaults()
                        serializers {
                            kregister(StationSerializer)
                            kregister(MenuLayoutSerializer)
                        }
                    }.buildAndLoadString(fileText)
                    stationNode.hint(StationSerializer.HINT_NODE, stationId)
                    val station = stationNode.krequire<Station>()
                    stations[stationId] = station

                } catch (e: Throwable) {
                    val message = "Can't register station: '${file.relativeTo(stationDir)}'"
                    if (RunningEnvironment.TEST.isRunning()) {
                        throw IllegalArgumentException(message, e)
                    }
                    logger.warn(message, e)
                }
            }

        logger.info("Registered stations: {}", stations.keys.joinToString())
    }

    override suspend fun onPostWorldAsync() {
        PluginEventBus.get().subscribe<StationRecipeLoadEvent> { loadStations() }
    }
}
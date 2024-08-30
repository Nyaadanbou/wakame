package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.eventbus.subscribe
import cc.mewcraft.wakame.gui.MenuLayoutSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.station.recipe.StationRecipeLoadEvent
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

object StationRegistry : Initializable, KoinComponent {
    private const val STATION_DIR_NAME = "station/stations"
    private val stations: MutableMap<String, Station> = mutableMapOf()

    /**
     * 获取所有合成站的唯一标识.
     */
    val NAMES: Set<String>
        get() = stations.keys

    fun find(id: String): Station? {
        return stations[id]
    }

    private val logger: Logger by inject()

    @VisibleForTesting
    fun loadStations() {
        stations.clear()

        val stationDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(STATION_DIR_NAME)
        stationDir.walk().drop(1).filter { it.extension == "yml" }.forEach { file ->
            try {
                val fileText = file.readText()
                val id = file.nameWithoutExtension

                val stationNode = yamlConfig {
                    withDefaults()
                    serializers {
                        kregister(StationSerializer)
                        kregister(MenuLayoutSerializer)
                    }
                }.buildAndLoadString(fileText)

                stationNode.hint(StationSerializer.HINT_NODE, id)
                val station = stationNode.krequire<Station>()
                stations[id] = station
                logger.info("Registered station: '${station.id}'")

            } catch (e: Throwable) {
                val message = "Can't register station: '${file.relativeTo(stationDir)}'"
                if (RunningEnvironment.TEST.isRunning()) {
                    throw IllegalArgumentException(message, e)
                }
                logger.warn(message, e)
            }

        }
    }

    override suspend fun onPostWorldAsync() {
        PluginEventBus.get().subscribe<StationRecipeLoadEvent> { loadStations() }
    }
}
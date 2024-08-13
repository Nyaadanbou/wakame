package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.eventbus.subscribe
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

object StationRegistry : KoinComponent {
    private const val STATION_DIR_NAME = "station/stations"
    val STATIONS: MutableMap<String, Station> = mutableMapOf()

    private val logger: Logger by inject()

    @VisibleForTesting
    fun loadConfig() {
        STATIONS.clear()

        val stationDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(STATION_DIR_NAME)
        stationDir.walk().drop(1).filter { it.extension == "yml" }.forEach { file ->
            try {
                val fileText = file.readText()
                val id = file.name

                val stationNode = yamlConfig {
                    withDefaults()
                    serializers {
                        kregister(StationSerializer)
                    }
                }.buildAndLoadString(fileText)

                stationNode.hint(StationSerializer.HINT_NODE, id)
                val station = stationNode.krequire<Station>()
                STATIONS[id] = station
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

    init {
        PluginEventBus.get().subscribe<StationRecipeLoadEvent> {
            loadConfig()
            TODO()
        }
    }
}
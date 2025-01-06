package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.ObjectMappers
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.File

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [CraftingStationRecipeRegistry::class]
)
@Reload(
    runAfter = [CraftingStationRecipeRegistry::class]
)
//@PostWorldDependency(
//    runBefore = [CraftingStationRecipeRegistry::class]
//)
//@ReloadDependency(
//    runBefore = [CraftingStationRecipeRegistry::class]
//)
internal object CraftingStationRegistry : KoinComponent {
    private const val STATION_DIR_NAME = "station/stations"
    private val stations: MutableMap<String, CraftingStation> = mutableMapOf()

    /**
     * 获取所有合成站的唯一标识.
     */
    val NAMES: Set<String>
        get() = stations.keys

    operator fun get(id: String): CraftingStation? {
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
                            registerAnnotatedObjects(ObjectMappers.DEFAULT)
                            kregister(StationSerializer)
                        }
                    }.buildAndLoadString(fileText)
                    stationNode.hint(StationSerializer.HINT_NODE, stationId)
                    val station = stationNode.krequire<CraftingStation>()
                    stations[stationId] = station

                } catch (e: Throwable) {
                    val message = "Can't register station: '${file.relativeTo(stationDir)}'"
                    if (RunningEnvironment.TEST.isRunning()) {
                        throw IllegalArgumentException(message, e)
                    }
                    LOGGER.warn(message, e)
                }
            }

        LOGGER.info("Registered stations: {}", stations.keys.joinToString())
    }

    @InitFun
    private fun onPostWorld() {
        loadStations()
    }

    @ReloadFun
    private fun onReload() {
        loadStations()
    }
}
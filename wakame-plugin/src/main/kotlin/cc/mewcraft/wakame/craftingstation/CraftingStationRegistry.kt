package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.qualifier.named
import java.io.File

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        CraftingStationRecipeRegistry::class, // deps: 需要直接的数据
    ]
)
@Reload(
    runAfter = [
        CraftingStationRecipeRegistry::class
    ]
)
internal object CraftingStationRegistry {
    private const val STATION_DIR_PATH = "station/stations"

    private val stations: MutableMap<String, CraftingStation> = mutableMapOf()

    /**
     * 获取所有合成站的唯一标识.
     */
    val NAMES: Set<String>
        get() = stations.keys

    @InitFun
    private fun init() = loadDataIntoRegistry()

    @ReloadFun
    private fun reload() = loadDataIntoRegistry()

    operator fun get(id: String): CraftingStation? {
        return stations[id]
    }

    @VisibleForTesting
    fun loadDataIntoRegistry() {
        stations.clear()

        val stationDir = Injector.get<File>(named(PLUGIN_DATA_DIR)).resolve(STATION_DIR_PATH)
        stationDir.walk()
            .drop(1)
            .filter { it.extension == "yml" }
            .forEach { file ->
                try {
                    val fileText = file.readText()
                    val stationId = file.nameWithoutExtension
                    val stationNode = buildYamlConfigLoader {
                        withDefaults()
                        serializers {
                            kregister(StationSerializer)
                        }
                    }.buildAndLoadString(fileText)
                    stationNode.hint(StationSerializer.HINT_NODE, stationId)
                    val station = stationNode.krequire<CraftingStation>()
                    stations[stationId] = station

                } catch (e: Throwable) {
                    Util.pauseInIde(IllegalStateException("Can't register station: '${file.relativeTo(stationDir)}'", e))
                }
            }

        LOGGER.info("Registered stations: {}", stations.keys.joinToString())
    }
}
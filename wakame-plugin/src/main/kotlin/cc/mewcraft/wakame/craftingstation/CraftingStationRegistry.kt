package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import org.jetbrains.annotations.VisibleForTesting

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

    private val stations: MutableMap<String, CraftingStation> = mutableMapOf()

    /**
     * 获取所有合成站的唯一标识.
     */
    val NAMES: Set<String>
        get() = stations.keys

    @InitFun
    fun init() = loadDataIntoRegistry()

    @ReloadFun
    fun reload() = loadDataIntoRegistry()

    operator fun get(id: String): CraftingStation? {
        return stations[id]
    }

    @VisibleForTesting
    fun loadDataIntoRegistry() {
        stations.clear()

        val stationDir = KoishDataPaths.CONFIGS
            .resolve(CraftingStationConstants.DATA_DIR)
            .resolve("stations")
            .toFile()
        stationDir.walk()
            .drop(1)
            .filter { it.extension == "yml" }
            .forEach { file ->
                try {
                    val fileText = file.readText()
                    val stationId = file.nameWithoutExtension
                    val stationNode = yamlLoader {
                        withDefaults()
                        serializers {
                            register(StationSerializer)
                        }
                    }.buildAndLoadString(fileText)
                    stationNode.hint(StationSerializer.HINT_NODE, stationId)
                    val station = stationNode.require<CraftingStation>()
                    stations[stationId] = station

                } catch (e: Throwable) {
                    Util.pauseInIde(IllegalStateException("Can't register station: '${file.relativeTo(stationDir)}'", e))
                }
            }

        LOGGER.info("Registered stations: {}", stations.keys.joinToString())
    }
}
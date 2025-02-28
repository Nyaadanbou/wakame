package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        RepairingTableRegistry::class, RecyclingStationRegistry::class // deps: 需要直接的数据
    ],
)
@Reload(
    runAfter = [
        RepairingTableRegistry::class, RecyclingStationRegistry::class
    ],
)
object BlacksmithStationRegistry {
    private val stations: MutableMap<String, BlacksmithStation> = mutableMapOf()

    val NAMES: Set<String>
        get() = stations.keys

    @InitFun
    private fun init() {
        load()
    }

    @ReloadFun
    private fun reload() {
        load()
    }

    fun getStation(id: String): BlacksmithStation? {
        return stations[id]
    }

    private fun load() {
        stations.clear()
        stations.putAll(BlacksmithStationSerializer.loadAllStations())
        stations.put("wtf", WtfBlacksmithStation)
    }
}
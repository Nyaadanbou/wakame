package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadableFun
import cc.mewcraft.wakame.reloader.ReloadableOrder

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [RepairingTableRegistry::class, RecyclingStationRegistry::class],
)
@Reload(
    order = ReloadableOrder.NORMAL,
    runBefore = [RepairingTableRegistry::class, RecyclingStationRegistry::class],
)
//@PostWorldDependency(runBefore = [RepairingTableRegistry::class, RecyclingStationRegistry::class])
//@ReloadDependency(runBefore = [RepairingTableRegistry::class, RecyclingStationRegistry::class])
object BlacksmithStationRegistry {
    private val stations: MutableMap<String, BlacksmithStation> = mutableMapOf()

    val names: Set<String>
        get() = stations.keys

    fun getStation(id: String): BlacksmithStation? {
        return stations[id]
    }

    private fun load() {
        stations.clear()
        stations.putAll(BlacksmithStationSerializer.loadAllStations())
        stations.put("wtf", WtfBlacksmithStation)
    }

    @InitFun
    private fun onPostWorld() {
        load()
    }

    @ReloadableFun
    private fun onReload() {
        load()
    }
}
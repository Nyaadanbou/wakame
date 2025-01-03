package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry

@Init(
    stage = InitStage.POST_WORLD,
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

//    override fun onReload() {
//        load()
//    }
}
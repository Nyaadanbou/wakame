package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry

@PostWorldDependency(runBefore = [RepairingTableRegistry::class, RecyclingStationRegistry::class])
@ReloadDependency(runBefore = [RepairingTableRegistry::class, RecyclingStationRegistry::class])
object BlacksmithStationRegistry : Initializable {
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

    override fun onPostWorld() {
        load()
    }

    override fun onReload() {
        load()
    }
}
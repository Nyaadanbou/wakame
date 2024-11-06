package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key

object RecyclingStationRegistry : Initializable {
    private val items: MutableMap<Key, PriceInstance> = mutableMapOf()
    private val stations: MutableMap<String, RecyclingStation> = mutableMapOf()

    fun getItem(id: Key): PriceInstance? {
        return items[id]
    }

    fun getStation(id: String): RecyclingStation? {
        return stations[id]
    }

    private fun load() {
        items.clear()
        items.putAll(RecyclingStationSerializer.loadAllItems())

        stations.clear()
        stations.putAll(RecyclingStationSerializer.loadAllStations())
    }

    override fun onPostWorld() {
        load()
    }

    override fun onReload() {
        load()
    }
}
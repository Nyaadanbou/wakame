package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting

object RecyclingStationRegistry : Initializable {
    private val items: MutableMap<Key, PriceInstance> = mutableMapOf()
    private val stations: MutableMap<String, RecyclingStation> = mutableMapOf()

    val names: Set<String>
        get() = stations.keys

    fun getItem(id: Key): PriceInstance? {
        return items[id]
    }

    fun getStation(id: String): RecyclingStation? {
        return stations[id]
    }

    @VisibleForTesting
    fun load() {
        items.clear()
        items.putAll(RecyclingStationSerializer.loadAllItems())

        stations.clear()
        stations.putAll(RecyclingStationSerializer.loadAllStations())
        stations.put("wtf", WtfRecyclingStation)
    }

    override fun onPostWorld() {
        load()
    }

    override fun onReload() {
        load()
    }
}
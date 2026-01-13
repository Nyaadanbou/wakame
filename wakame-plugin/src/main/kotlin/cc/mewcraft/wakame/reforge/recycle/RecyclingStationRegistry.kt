package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting

@Init(InitStage.POST_WORLD)
object RecyclingStationRegistry {
    private val items: MutableMap<Key, PriceInstance> = mutableMapOf()
    private val stations: MutableMap<String, RecyclingStation> = mutableMapOf()

    val NAMES: Set<String>
        get() = stations.keys

    @InitFun
    fun init() {
        load()
    }

    fun reload() {
        load()
    }

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
}
package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadableFun
import cc.mewcraft.wakame.reloader.ReloadableOrder
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting

@Init(
    stage = InitStage.POST_WORLD
)
@Reload(
    order = ReloadableOrder.NORMAL
)
object RecyclingStationRegistry {
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

    @InitFun
    private fun onPostWorld() {
        load()
    }

    @ReloadableFun
    private fun onReload() {
        load()
    }
}
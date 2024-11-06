package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key

internal object RecyclingStationSerializer {
    private const val DIR_NAME = "recycle"

    fun loadAllItems(): Map<Key, PriceInstance> {
        // TODO #227
        return emptyMap()
    }

    fun loadAllStations(): Map<String, RecyclingStation> {
        // TODO #227
        return emptyMap()
    }
}
package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import org.koin.core.component.get
import org.slf4j.Logger

internal object RecyclingStationSerializer {
    private const val DIR_NAME = "recycle"
    private val logger: Logger = Injector.get()

    fun loadAllItems(): Map<Key, PriceInstance> {
        // TODO #227
        return emptyMap()
    }

    fun loadAllStations(): Map<String, RecyclingStation> {
        // TODO #227
        return emptyMap()
    }
}
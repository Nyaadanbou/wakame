package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

interface RepairingTable {
    val title: Component
    val priceMap: PriceMap

    interface PriceMap {
        fun get(key: Key): PriceInstance?
    }
}
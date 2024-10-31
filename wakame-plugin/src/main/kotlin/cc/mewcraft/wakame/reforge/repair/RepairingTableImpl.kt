package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*

internal object WtfRepairingTable : RepairingTable {
    override val title: Component = text("Repairing Station (Cheat ON)")
    override val priceMap: RepairingTable.PriceMap = PriceMap

    private object PriceMap : RepairingTable.PriceMap {
        private val ZERO_PRICE_INSTANCE = PriceInstance(.0, emptyMap())

        override fun get(key: Key): PriceInstance {
            return ZERO_PRICE_INSTANCE
        }
    }
}

internal class SimpleRepairingTable(
    override val title: Component,
    override val priceMap: RepairingTable.PriceMap,
) : RepairingTable {

}
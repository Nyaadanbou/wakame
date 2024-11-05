package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import kotlin.random.Random

internal object WtfRecyclingStation : RecyclingStation {
    override val title: Component = text("Recycling Station (Cheat ON)")
    override val random: Random = Random(123)
    override val priceMap: RecyclingStation.PriceMap = PriceMap

    private object PriceMap : RecyclingStation.PriceMap {
        private val ZERO_PRICE_INSTANCE = PriceInstance(.0, emptyMap())

        override fun get(key: Key): PriceInstance {
            return ZERO_PRICE_INSTANCE
        }
    }
}

internal class SimpleRecyclingStation(
    override val title: Component,
    override val priceMap: RecyclingStation.PriceMap,
) : RecyclingStation {
    override val random: Random = Random(0)
}
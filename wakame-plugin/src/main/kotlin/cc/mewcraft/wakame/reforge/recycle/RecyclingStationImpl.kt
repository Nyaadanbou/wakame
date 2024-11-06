package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.examination.Examinable
import kotlin.random.Random

internal data object WtfRecyclingStation : RecyclingStation {
    override val id: String = "wtf"
    override val title: Component = text("Recycling Station (Cheat ON)")
    override val random: Random = Random(123)

    private val ZERO_PRICE_INSTANCE = PriceInstance(.0, emptyMap())

    override fun getPrice(key: Key): PriceInstance {
        return ZERO_PRICE_INSTANCE
    }
}

internal class SimpleRecyclingStation(
    override val id: String,
    override val title: Component,
    private val items: Set<Key>,
) : RecyclingStation, Examinable {
    override val random: Random = Random(0)

    override fun getPrice(key: Key): PriceInstance? {
        if (key !in items) {
            return null
        }

        return RecyclingStationRegistry.getItem(key)
    }
}
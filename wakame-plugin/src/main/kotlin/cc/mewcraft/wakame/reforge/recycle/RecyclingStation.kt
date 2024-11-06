package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import kotlin.random.Random

interface RecyclingStation {
    val id: String
    val title: Component
    val random: Random

    fun getPrice(key: Key): PriceInstance?
}
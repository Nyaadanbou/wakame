package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import kotlin.random.Random

interface RecyclingStation : Examinable {
    val id: String
    val enabled: Boolean
    val title: Component
    val random: Random

    fun getPrice(key: Key): PriceInstance?
}
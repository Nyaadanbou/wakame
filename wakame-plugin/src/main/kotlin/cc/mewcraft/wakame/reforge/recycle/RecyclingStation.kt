package cc.mewcraft.wakame.reforge.recycle

import net.kyori.adventure.text.Component
import kotlin.random.Random

interface RecyclingStation {
    val title: Component
    val random: Random
}
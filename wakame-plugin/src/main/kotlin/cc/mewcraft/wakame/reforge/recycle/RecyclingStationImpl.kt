package cc.mewcraft.wakame.reforge.recycle

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import kotlin.random.Random

internal object WtfRecyclingStation : RecyclingStation {
    override val title: Component = text("Recycling Station (Cheat ON)")
    override val random: Random
        get() = Random
}

internal class SimpleRecyclingStation(
    override val title: Component,
) : RecyclingStation {
    override val random: Random = Random(0)
}
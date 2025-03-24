package cc.mewcraft.wakame.util.adventure

import net.kyori.adventure.sound.Sound

fun sound(block: Sound.Builder.() -> Unit): Sound {
    return Sound.sound().apply(block).build()
}
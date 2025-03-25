package cc.mewcraft.wakame.util.adventure

import io.papermc.paper.math.FinePosition
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound

fun sound(block: Sound.Builder.() -> Unit): Sound {
    return Sound.sound().apply(block).build()
}

fun Audience.playSound(block: Sound.Builder.() -> Unit) {
    this.playSound(sound(block))
}

fun Audience.playSound(x: Double, y: Double, z: Double, block: Sound.Builder.() -> Unit) {
    this.playSound(sound(block), x, y, z)
}

fun Audience.playSound(location: FinePosition, block: Sound.Builder.() -> Unit) {
    this.playSound(location.x(), location.y(), location.z(), block)
}

fun Audience.playSound(emitter: Sound.Emitter, block: Sound.Builder.() -> Unit) {
    this.playSound(sound(block), emitter)
}
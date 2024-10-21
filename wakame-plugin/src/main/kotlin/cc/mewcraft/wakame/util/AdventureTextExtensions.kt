package cc.mewcraft.wakame.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

fun Component.colorRecursively(color: TextColor): Component {
    return this.color(color).children(
        this.children().map { child -> child.colorRecursively(color) }
    ).compact()
}

fun Component.decorateRecursively(decoration: TextDecoration): Component {
    return this.decorate(decoration).children(
        this.children().map { child -> child.decorateRecursively(decoration) }
    ).compact()
}
package cc.mewcraft.wakame.util.adventure

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

val Component.plain: String
    get() = PlainTextComponentSerializer.plainText().serialize(this)

val List<Component>.plain: List<String>
    get() = map(Component::plain)

val Component.removeItalic: Component
    get() = decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

val List<Component>.removeItalic: List<Component>
    get() = map(Component::removeItalic)

fun Component.fontRecursively(font: Key): Component {
    return this.font(font).children(
        this.children().map { child -> child.fontRecursively(font) }
    ).compact()
}

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

fun Component.styleRecursively(style: Style): Component {
    return this.style(style).children(
        this.children().map { child -> child.styleRecursively(style) }
    ).compact()
}

fun Component.styleRecursively(style: Style, strategy: Style.Merge.Strategy): Component {
    return this.style { it.merge(style, strategy) }.children(
        this.children().map { child -> child.styleRecursively(style) }
    ).compact()
}


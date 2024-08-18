package cc.mewcraft.wakame.util

import net.kyori.adventure.text.Component
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
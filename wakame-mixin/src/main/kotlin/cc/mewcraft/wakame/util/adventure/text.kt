package cc.mewcraft.wakame.util.adventure

import cc.mewcraft.wakame.MM
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BuildableComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.nbt.StringTag
import org.bukkit.ChatColor
import java.awt.Color


fun Component.toJson(): String {
    return GsonComponentSerializer.gson().serialize(this)
}

fun Component.toNBT(): StringTag {
    return StringTag.valueOf(toJson())
}

fun Component.font(font: String): Component {
    return font(Key.key(font))
}

fun Component.fontName(): String? {
    return font()?.toString()
}

val Component.plain: String
    get() = PlainTextComponentSerializer.plainText().serialize(this)

val List<Component>.plain: List<String>
    get() = map(Component::plain)

val Component.removeItalic: Component
    get() = decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

val List<Component>.removeItalic: List<Component>
    get() = map(Component::removeItalic)

private val legacyComponentSerializer: LegacyComponentSerializer = LegacyComponentSerializer.builder()
    .hexColors()
    .useUnusualXRepeatedCharacterHexFormat()
    .extractUrls()
    .build()
private val newlineRegex: Regex = "(?<!\\\\)\\\\n".toRegex()
private val escapedAngleBracketRegex: Regex = "\\\\<".toRegex()
private val legacyMiniMessage: MiniMessage = MiniMessage.builder()
    .preProcessor { input ->
        val input2: String = ChatColor.translateAlternateColorCodes('&', input).replace(newlineRegex, "\n")
        val input3: TextComponent = legacyComponentSerializer.deserialize(input2)
        val input4: String = MM.serialize(input3)
        input4.replace(escapedAngleBracketRegex, "<")
    }
    .build()

val String.legacyMini: Component
    get() = legacyMiniMessage.deserialize(this)

val String.legacy: Component
    get() = legacyComponentSerializer.deserialize(this)

val List<String>.legacyMini: List<Component>
    get() = map(legacyMiniMessage::deserialize)

val List<String>.legacy: List<Component>
    get() = map(legacyComponentSerializer::deserialize)

fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.font(font: String): B {
    return font(Key.key(font))
}

fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.color(color: Color): B {
    return color(TextColor.color(color.rgb))
}

internal fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.indent(spaces: Int): B {
    return append(Component.text(" ".repeat(spaces)))
}

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


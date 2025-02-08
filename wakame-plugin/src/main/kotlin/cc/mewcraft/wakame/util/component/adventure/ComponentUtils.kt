@file:Suppress("DEPRECATION")

package cc.mewcraft.wakame.util.component.adventure

import cc.mewcraft.wakame.util.REGISTRY_ACCESS
import cc.mewcraft.wakame.util.toResourceLocation
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BuildableComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import java.awt.Color
import java.util.*
import net.minecraft.network.chat.Component as MojangComponent
import net.minecraft.network.chat.Style as MojangStyle

fun MojangComponent.toAdventureComponent(): Component {
    return PaperAdventure.asAdventure(this)
}

fun MojangComponent.toJson(): String {
    return MojangComponent.Serializer.toJson(this, REGISTRY_ACCESS)
}

fun Component.toNMSComponent(): MojangComponent {
    return PaperAdventure.asVanilla(this)
}

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

private val DEFAULT_STYLE = MojangStyle.EMPTY
    .withColor(0xFFFFFF)
    .withBold(false)
    .withItalic(false)
    .withUnderlined(false)
    .withStrikethrough(false)
    .withObfuscated(false)

fun MojangComponent.withoutPreFormatting(): MojangComponent {
    return MojangComponent.literal("")
        .withStyle(DEFAULT_STYLE)
        .append(this)
}

fun Style.toNmsStyle(): MojangStyle {
    var style = MojangStyle.EMPTY
    color()?.let { style = style.withColor(it.value()) }
    font()?.let { style = style.withFont(it.toResourceLocation()) }

    when (decoration(TextDecoration.BOLD)) {
        TextDecoration.State.TRUE -> style = style.withBold(true)
        TextDecoration.State.FALSE -> style = style.withBold(false)
        else -> Unit
    }

    when (decoration(TextDecoration.ITALIC)) {
        TextDecoration.State.TRUE -> style = style.withItalic(true)
        TextDecoration.State.FALSE -> style = style.withItalic(false)
        else -> Unit
    }

    when (decoration(TextDecoration.UNDERLINED)) {
        TextDecoration.State.TRUE -> style = style.withUnderlined(true)
        TextDecoration.State.FALSE -> style = style.withUnderlined(false)
        else -> Unit
    }

    when (decoration(TextDecoration.STRIKETHROUGH)) {
        TextDecoration.State.TRUE -> style = style.withStrikethrough(true)
        TextDecoration.State.FALSE -> style = style.withStrikethrough(false)
        else -> Unit
    }

    when (decoration(TextDecoration.OBFUSCATED)) {
        TextDecoration.State.TRUE -> style = style.withObfuscated(true)
        TextDecoration.State.FALSE -> style = style.withObfuscated(false)
        else -> Unit
    }

    return style
}

internal fun MojangComponent.isEmpty(): Boolean {
    val queue = LinkedList<MojangComponent>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val current = queue.poll()

        when (val contents = current.contents) {
            is PlainTextContents -> {
                if (contents.text().isNotEmpty())
                    return false
            }

            is TranslatableContents -> {
                if (contents.key.isNotEmpty() || !contents.fallback.isNullOrEmpty())
                    return false

                for (arg in contents.args) {
                    if (arg is MojangComponent) {
                        queue.add(arg)
                    }
                }
            }

            else -> return false // TODO: support other content types
        }

        queue.addAll(current.siblings)
    }

    return true
}

fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.font(font: String): B {
    return font(Key.key(font))
}

fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.color(color: Color): B {
    return color(TextColor.color(color.rgb))
}

internal fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.indent(spaces: Int): B {
    return append(Component.text(" ".repeat(spaces)))
} 
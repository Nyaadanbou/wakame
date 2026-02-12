package cc.mewcraft.wakame.util.adventure

import cc.mewcraft.wakame.util.toIdentifier
import com.mojang.serialization.JsonOps
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import java.util.*
import net.minecraft.network.chat.Component as MojangComponent

fun MojangComponent.toAdventureComponent(): Component {
    return PaperAdventure.asAdventure(this)
}

fun MojangComponent.toJson(): String {
    return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, this).orThrow.asString
}

fun Component.toNMSComponent(): MojangComponent {
    return PaperAdventure.asVanilla(this)
}

private val DEFAULT_STYLE = net.minecraft.network.chat.Style.EMPTY
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

fun Style.toNmsStyle(): net.minecraft.network.chat.Style {
    var style = net.minecraft.network.chat.Style.EMPTY
    color()?.let { style = style.withColor(it.value()) }
    font()?.let { style = style.withFont(FontDescription.Resource(it.toIdentifier())) }

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

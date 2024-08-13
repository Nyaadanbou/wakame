package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.lang.GlobalTranslation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.entity.Player
import java.util.Locale

fun Component.translateBy(locale: Locale): Component {
    if (this !is TranslatableComponent)
        return this
    return GlobalTranslation.translate(this, locale) ?: this
}

fun Component.translateBy(viewer: Player): Component {
    return translateBy(viewer.locale())
}
package cc.mewcraft.wakame.gui.common

import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.NamedTextColor

object GuiMessages {
    @JvmField
    val MESSAGE_CANCELLED = text { content("猫咪不可以!"); color(NamedTextColor.RED) }
}
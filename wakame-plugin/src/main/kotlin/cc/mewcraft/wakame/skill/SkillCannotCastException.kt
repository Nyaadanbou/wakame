package cc.mewcraft.wakame.skill

import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

sealed class SkillCannotCastException: Throwable()

fun SkillCannotCastException.beautify(): Component {
    return this.message.mini.color(NamedTextColor.RED)
}

class NoTargetException(
    override val message: String = "No target to cast skill"
): SkillCannotCastException()
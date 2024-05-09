package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

sealed class BinarySkillCore : BinaryCore {
    abstract val instance: ConfiguredSkill
    abstract val trigger: SkillTrigger

    override fun provideDisplayLore(): LoreLine {
        TODO("cell-overhaul")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", instance.key))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

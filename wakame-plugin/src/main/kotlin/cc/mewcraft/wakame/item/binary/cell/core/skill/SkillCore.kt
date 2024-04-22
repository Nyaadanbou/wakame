package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.SkillTrigger

sealed interface BinarySkillCore : BinaryCore {
    val instance: ConfiguredSkill
    val trigger: SkillTrigger
}

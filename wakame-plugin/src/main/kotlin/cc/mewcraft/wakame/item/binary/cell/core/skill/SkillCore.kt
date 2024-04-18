package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.SkillInstance
import cc.mewcraft.wakame.item.SkillTrigger
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore

sealed interface BinarySkillCore : BinaryCore {
    val instance: SkillInstance
    val trigger: SkillTrigger
}

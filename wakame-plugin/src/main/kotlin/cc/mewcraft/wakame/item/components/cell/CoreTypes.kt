package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.wakame.item.components.cell.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cell.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cell.cores.noop.CoreNoop
import cc.mewcraft.wakame.item.components.cell.cores.skill.CoreSkill

object CoreTypes {
    val EMPTY: CoreType<CoreEmpty> = CoreEmpty
    val ATTRIBUTE: CoreType<CoreAttribute> = CoreAttribute.Type
    val SKILL: CoreType<CoreSkill> = CoreSkill.Type
    val NOP: CoreType<CoreNoop> = CoreNoop

    private fun <T : Core> dummy(): CoreType<T> {
        TODO()
    }
}
package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

data object CurseMatchRuleAny : CurseMatchRule {
    override fun test(curse: Curse): Boolean = true
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of()
    override fun toString(): String = toSimpleString()
}
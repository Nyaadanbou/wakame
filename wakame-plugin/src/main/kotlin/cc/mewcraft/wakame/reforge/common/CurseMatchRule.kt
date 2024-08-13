package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 词条栏的诅咒的匹配规则, 用于测试一个诅咒是否符合某种规则.
 */
interface CurseMatchRule : Examinable {
    fun test(curse: Curse): Boolean
}


/* Implementations */


data object CurseMatchRuleAny : CurseMatchRule {
    override fun test(curse: Curse): Boolean = true
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of()
    override fun toString(): String = toSimpleString()
}
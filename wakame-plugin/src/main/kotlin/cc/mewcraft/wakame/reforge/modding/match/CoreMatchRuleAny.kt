package cc.mewcraft.wakame.reforge.modding.match

import cc.mewcraft.wakame.item.components.cells.Core
import java.util.regex.Pattern

/**
 * 可以匹配所有核心的匹配规则.
 */
object CoreMatchRuleAny : CoreMatchRule {
    override val path: Pattern = "[a-z0-9/._-]+".toPattern()
    override val priority: Int = Int.MIN_VALUE
    override fun test(core: Core): Boolean = true // 永远返回 true
}
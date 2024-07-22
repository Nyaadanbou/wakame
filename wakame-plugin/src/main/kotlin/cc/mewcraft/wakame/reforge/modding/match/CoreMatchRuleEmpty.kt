package cc.mewcraft.wakame.reforge.modding.match

import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import java.util.regex.Pattern

/**
 * 用于匹配空核心. 可能用不到?
 */
object CoreMatchRuleEmpty : CoreMatchRule {
    override val path: Pattern = "empty".toPattern()
    override val priority: Int = Int.MIN_VALUE + 1
    override fun test(core: Core): Boolean {
        return core is CoreEmpty
    }
}

package cc.mewcraft.wakame.reforge.modding

import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import java.util.regex.Pattern
import java.util.stream.Stream

/**
 * 用于测试空核心. 可能用不到?
 */
data object CoreMatchRuleEmpty : CoreMatchRule {
    override val path: Pattern = "empty".toPattern()
    override val priority: Int = Int.MIN_VALUE + 1
    override fun test(core: Core): Boolean {
        return core is CoreEmpty
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("path", path),
        ExaminableProperty.of("priority", priority),
    )

    override fun toString(): String = toSimpleString()
}

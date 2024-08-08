package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import java.util.regex.Pattern
import java.util.stream.Stream

/**
 * 可以匹配所有核心的匹配规则.
 */
data object CoreMatchRuleAny : CoreMatchRule {
    override val path: Pattern = "[a-z0-9/._-]+".toPattern()
    override val priority: Int = Int.MIN_VALUE
    override fun test(core: Core): Boolean = true // 永远返回 true

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("path", path),
        ExaminableProperty.of("priority", priority),
    )

    override fun toString(): String = toSimpleString()
}
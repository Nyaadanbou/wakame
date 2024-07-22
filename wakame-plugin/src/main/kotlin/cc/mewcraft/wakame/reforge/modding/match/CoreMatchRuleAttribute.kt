package cc.mewcraft.wakame.reforge.modding.match

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import java.util.regex.Pattern

/**
 * 用于测试属性核心.
 */
class CoreMatchRuleAttribute(
    override val path: Pattern,
    val operation: AttributeModifier.Operation,
    val element: Element?,
) : CoreMatchRule {
    override val priority: Int = 1

    override fun test(core: Core): Boolean {
        // 开发日记 2024/7/19
        // 正则表达式可以写任意的东西, 使得这里的 Core
        // 可能不是一个 CoreAttribute, 但是这里的代码
        // 仍然假设 core 是一个 CoreAttribute, 这可能
        // 会导致运行时错误.
        //
        // 解决方案:
        // 单独处理 Any Core 的情况, 设计一个专门的实现.
        // 这里只负责处理已经是 CoreAttribute 的情况.

        if (core !is CoreAttribute) {
            return false
        }

        val matcher = path.matcher(core.key.value())
        if (!matcher.matches()) {
            return false
        }

        return operation == core.operation && element == core.element
    }
}

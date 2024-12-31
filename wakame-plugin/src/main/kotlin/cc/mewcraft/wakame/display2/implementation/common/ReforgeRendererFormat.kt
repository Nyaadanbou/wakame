// 文件说明:
// 在不同重铸系统中通用的 [RendererFormat]

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.util.styleRecursively
import net.kyori.adventure.extra.kotlin.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.format.Style
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 本 class 封装了共同逻辑, 用于渲染将要被重铸的核孔.
 *
 * @param style 应用在核心上的样式
 * @param prefix 应用在核心上的前缀
 * @param suffix 应用在核心上的后缀
 */
@ConfigSerializable
internal data class ReforgeDifferenceFormat(
    // 我们想让 style 的默认值含义是 [不修改核心原有的样式].
    // 经验证, 不能用 Style.empty(), 因为会清空原有样式.
    // 所以这里用 null 来表示 [不修改核心原有的样式].
    val style: Style? = null,
    val prefix: Component = empty(),
    val suffix: Component = empty(),
) {
    fun process(source: List<Component>): List<Component> {
        return source.map { text -> (prefix + (style?.let { text.styleRecursively(it) } ?: text) + suffix).compact() }
    }
}

// 文件说明:
// 在不同系统中通用的 [AttributeCore] 的实现

package cc.mewcraft.wakame.item.display.implementation.common

import cc.mewcraft.wakame.entity.attribute.bundle.element
import cc.mewcraft.wakame.item.data.impl.AttributeCore
import cc.mewcraft.wakame.item.display.DerivedIndex
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 用于生成 [AttributeCore] 的 [cc.mewcraft.wakame.item.display.DerivedIndex].
 */
internal fun AttributeCore.computeIndex(namespace: String): DerivedIndex {
    val indexId = buildString {
        val wrapped2 = wrapped
        append(wrapped2.id)
        append('.')
        append(wrapped2.operation.key)
        wrapped2.element?.let {
            append('.')
            append(it.getIdAsString())
        }
    }
    return Key.key(namespace, indexId)
}

/**
 * 记录了 [AttributeCore] 特定内容的相对位置.
 */
@ConfigSerializable
internal data class AttributeCoreOrdinalFormat(
    val element: List<String>,
    val operation: List<String>,
)

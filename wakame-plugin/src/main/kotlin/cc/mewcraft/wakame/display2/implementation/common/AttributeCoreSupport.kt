// 文件说明:
// 在不同系统中通用的 [AttributeCore] 的实现

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

/**
 * 用于生成 [AttributeCore] 的 [DerivedIndex].
 */
internal fun AttributeCore.computeIndex(namespace: String): DerivedIndex {
    val attribute = attribute
    val indexId = buildString {
        append(attribute.id)
        append('.')
        append(attribute.operation.key)
        attribute.element?.let {
            append('.')
            append(it.uniqueId)
        }
    }
    return Key.key(namespace, indexId)
}

/**
 * 记录了 [AttributeCore] 特定内容的相对位置.
 */
@ConfigSerializable
internal data class AttributeCoreOrdinalFormat(
    @Setting @Required
    val element: List<String>,
    @Setting @Required
    val operation: List<String>,
)

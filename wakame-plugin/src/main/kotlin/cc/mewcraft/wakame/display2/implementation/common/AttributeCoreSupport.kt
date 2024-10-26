package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import net.kyori.adventure.key.Key

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

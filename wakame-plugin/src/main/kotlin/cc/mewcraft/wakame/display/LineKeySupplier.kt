package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.util.Key

@InternalApi("Use the subclasses instead")
internal sealed interface LineKeySupplier<T> {
    /**
     * 根据某种规则为特定的 [obj] 生成唯一的标识。
     *
     * **该函数会特别返回 [SKIP_RENDERING] 用来表示 [obj] 不应该被渲染。你应该对返回值做检查，以确保不渲染标记为 [SKIP_RENDERING] 的内容。**
     *
     * 你可以用该函数所返回的 [FullKey] 配合 [LoreMetaLookup] 找到其在 Item Lore 中的顺序。
     *
     * @return [obj] 的唯一标识
     */
    fun get(obj: T): FullKey
}

/**
 * Signals a rendering should be skipped.
 *
 * It is to be used to compare by reference.
 */
val SKIP_RENDERING: FullKey = Key("renderer", "noop")

@OptIn(InternalApi::class)
internal interface AbilityKeySupplier : LineKeySupplier<BinaryAbilityCore>

@OptIn(InternalApi::class)
internal interface AttributeKeySupplier : LineKeySupplier<BinaryAttributeCore>

@OptIn(InternalApi::class)
internal interface ItemMetaKeySupplier : LineKeySupplier<BinaryItemMeta<*>>
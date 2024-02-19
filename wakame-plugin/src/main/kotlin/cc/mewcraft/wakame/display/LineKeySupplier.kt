package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.scheme.meta.SchemeMeta
import kotlin.reflect.KClass

@InternalApi("Use the subclasses instead")
internal sealed interface LineKeySupplier<T> {
    /**
     * 根据某种规则为特定的 [obj] 生成唯一的标识。
     *
     * 你可以用该函数所返回的 [FullKey] 配合 [LoreIndexLookup] 找到其在 Item Lore 中的顺序。
     */
    fun get(obj: T): FullKey
}

@OptIn(InternalApi::class)
internal interface AbilityLineKeySupplier : LineKeySupplier<BinaryAbilityCore>

@OptIn(InternalApi::class)
internal interface AttributeLineKeySupplier : LineKeySupplier<BinaryAttributeCore>

@OptIn(InternalApi::class)
internal interface MetaLineKeySupplier : LineKeySupplier<KClass<out SchemeMeta<*>>>
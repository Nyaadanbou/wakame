package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.scheme.meta.SchemeMeta
import net.kyori.adventure.key.Key
import kotlin.reflect.KClass

@InternalApi("Use the subclasses instead")
internal sealed interface LineKeySupplier<T> {
    /**
     * 根据某种规则为 [value] 生成唯一的标识。
     *
     * 实现上，通过此标识就可以找到一行文本内容在 lore 中的顺序。
     *
     * @see LineIndexSupplier.getIndex
     */
    fun getKey(value: T): Key
}

@OptIn(InternalApi::class)
internal interface AbilityLineKeySupplier : LineKeySupplier<BinaryAbilityCore>

@OptIn(InternalApi::class)
internal interface AttributeLineKeySupplier : LineKeySupplier<BinaryAttributeCore>

@OptIn(InternalApi::class)
internal interface MetaLineKeySupplier : LineKeySupplier<KClass<SchemeMeta<*>>>
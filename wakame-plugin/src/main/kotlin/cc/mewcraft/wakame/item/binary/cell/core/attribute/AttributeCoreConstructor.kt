package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

//
// 构造函数，用于创建对象
//

/**
 * A constructor function to create [BinaryAttributeCore].
 *
 * The given [compound] must be in the following format:
 *
 * (An example of many)
 *
 * ```
 * TBD.
 * ```
 */
fun BinaryAttributeCore(
    compound: CompoundTag,
): BinaryAttributeCore {
    val key = Key(compound.getString(CoreBinaryKeys.CORE_IDENTIFIER))
    val facade = AttributeRegistry.FACADES[key]
    val core = facade.binaryCoreCreatorByTag(compound)
    return core
}

/**
 * A constructor function to create [BinaryAttributeCore].
 *
 * The given [node] must be in the following format:
 *
 * (An example of many)
 *
 * ```yaml
 * key: <key>
 * operation: <operation>
 * lower: <double>
 * upper: <double>
 * element: <element>
 * ```
 */
fun BinaryAttributeCore(
    node: ConfigurationNode,
): BinaryAttributeCore {
    val key = node.node("key").krequire<Key>()
    val facade = AttributeRegistry.FACADES[key]
    val core = facade.binaryCoreCreatorByConfig(node)
    return core
}

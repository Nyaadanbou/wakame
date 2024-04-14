@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.item.schema.cell.core.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.AttributeData
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A constructor function to create [SchemaAttributeCore].
 *
 * @return a new instance of [SchemaAttributeCore]
 */
fun SchemaAttributeCore(node: ConfigurationNode): SchemaAttributeCore {
    val key = node.node("key").krequire<Key>()
    val schemaEncoder = AttributeRegistry.FACADES[key].SCHEMA_CORE_NODE_ENCODER
    val schemaAttributeCore = schemaEncoder.encode(node)
    return schemaAttributeCore
}

/**
 * Represents a [SchemaCore] of an attribute.
 *
 * This interface specifically extends [AttributeComponent.Op] because the `operation` property
 * is, by design, shared by all subclasses of [SchemaAttributeCore] without any exceptions.
 */
sealed interface SchemaAttributeCore : SchemaCore, AttributeData, AttributeComponent.Op<AttributeModifier.Operation> {
    /**
     * The numeric value's tag type.
     *
     * It's used to generate most optimized numeric values for NBT data.
     */
    val tagType: ShadowTagType

    /**
     * Specifically overrides the return type as [BinaryAttributeCore].
     */
    override fun generate(context: SchemaGenerationContext): BinaryAttributeCore
}

//
// 下面是可能会用到的扩展函数
//

val SchemaAttributeCore.element: Element
    get() = requireNotNull(elementOrNull) { "The 'element' component is not present" }
val SchemaAttributeCore.elementOrNull: Element?
    get() = (this as? AttributeComponent.Element<Element>)?.element

val SchemaAttributeCore.value: RandomizedValue
    get() = requireNotNull(valueOrNull) { "The 'value' component is not present" }
val SchemaAttributeCore.valueOrNull: RandomizedValue?
    get() = (this as? AttributeComponent.Single<RandomizedValue>)?.value

val SchemaAttributeCore.lower: RandomizedValue
    get() = requireNotNull(lowerOrNull) { "The 'lower' component is not present" }
val SchemaAttributeCore.lowerOrNull: RandomizedValue?
    get() = (this as? AttributeComponent.Ranged<RandomizedValue>)?.lower

val SchemaAttributeCore.upper: RandomizedValue
    get() = requireNotNull(upperOrNull) { "The 'upper' component is not present" }
val SchemaAttributeCore.upperOrNull: RandomizedValue?
    get() = (this as? AttributeComponent.Ranged<RandomizedValue>)?.upper
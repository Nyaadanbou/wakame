@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.item.schema.cell.core.attribute

import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.AttributeData
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode

/**
 * A constructor function to create [SchemaAttributeCore].
 *
 * @return a new instance of [SchemaAttributeCore]
 */
fun SchemaAttributeCore(node: ConfigurationNode): SchemaAttributeCore {
    val key = node.node("key").krequire<Key>()
    val schemaAttributeCore = AttributeRegistry.FACADES[key].schemaCoreCreatorByConfig(node)
    // return schemaAttributeCore
    TODO("to be deleted")
}

/**
 * Represents a [SchemaCore] of an attribute.
 *
 * This interface specifically extends [AttributeComponent.Op] because the `operation` property
 * is, by design, shared by all subclasses of [SchemaAttributeCore] without any exceptions.
 */
sealed interface SchemaAttributeCore : SchemaCore, AttributeData, AttributeComponent.Op, Examinable {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore
}

/* Some useful extension functions */

val SchemaAttributeCore.element: Element
    get() = requireNotNull(elementOrNull) { "The 'element' component is not present" }
val SchemaAttributeCore.elementOrNull: Element?
    get() = (this as? AttributeComponent.Element)?.element

val SchemaAttributeCore.value: RandomizedValue
    get() = requireNotNull(valueOrNull) { "The 'value' component is not present" }
val SchemaAttributeCore.valueOrNull: RandomizedValue?
    get() = (this as? AttributeComponent.Fixed<RandomizedValue>)?.value

val SchemaAttributeCore.lower: RandomizedValue
    get() = requireNotNull(lowerOrNull) { "The 'lower' component is not present" }
val SchemaAttributeCore.lowerOrNull: RandomizedValue?
    get() = (this as? AttributeComponent.Ranged<RandomizedValue>)?.lower

val SchemaAttributeCore.upper: RandomizedValue
    get() = requireNotNull(upperOrNull) { "The 'upper' component is not present" }
val SchemaAttributeCore.upperOrNull: RandomizedValue?
    get() = (this as? AttributeComponent.Ranged<RandomizedValue>)?.upper
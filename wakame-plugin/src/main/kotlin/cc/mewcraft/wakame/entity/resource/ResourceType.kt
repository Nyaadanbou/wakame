package cc.mewcraft.wakame.entity.resource

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes

/**
 * The registry of all resource types.
 *
 * You can use it to get an instance of [ResourceType].
 */
object ResourceTypeRegistry {

    @JvmStatic
    val MANA = object : ResourceType {
        override fun initialAmount(attributeMap: AttributeMap): Int = 0
        override fun maximumAmount(attributeMap: AttributeMap): Int = attributeMap.getValue(Attributes.MAX_MANA).toInt()
        override fun toString(): String = "ResourceType[MANA]"
    }

}

/**
 * Represents a type of resource owned by a subject.
 *
 * You can use [ResourceTypeRegistry] to get an instance of [ResourceType].
 *
 * @see ResourceTypeRegistry
 */
interface ResourceType {
    /**
     * Computes the initial amount of the resource.
     */
    fun initialAmount(attributeMap: AttributeMap): Int

    /**
     * Computes the maximum amount of the resource.
     */
    fun maximumAmount(attributeMap: AttributeMap): Int
}

package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.user.User

/**
 * Creates a new [ResourceMap].
 */
fun ResourceMap(user: User<*>): ResourceMap {
    return PlayerResourceMap(user.attributeMap)
}

/**
 * A Resource Map owned by a player.
 */
private class PlayerResourceMap(
    private val attributeMap: AttributeMap,
) : ResourceMap {
    private val data: Map<ResourceType, Resource> = buildMap {
        // Here, you define the available resource types for the player.

        // Side note: we better NOT to make it configurable in file as a new
        // resource type not only is a bunch of states, but it also involves
        // other mechanism to update and use the states of the resource.

        this[ResourceTypeRegistry.MANA] = Resource(ResourceTypeRegistry.MANA, attributeMap)
    }

    override fun current(type: ResourceType): Int {
        return getResource(type).current()
    }

    override fun maximum(type: ResourceType): Int {
        return getResource(type).maximum()
    }

    override fun add(type: ResourceType, value: Int): Boolean {
        return getResource(type).add(value)
    }

    override fun take(type: ResourceType, value: Int): Boolean {
        return getResource(type).take(value)
    }

    override fun set(type: ResourceType, value: Int): Boolean {
        return getResource(type).set(value)
    }

    private fun getResource(type: ResourceType): Resource {
        return requireNotNull(data[type]) { "The resource type $type" }
    }
}

/**
 * The object stores the amount of resource of a specific type.
 */
private class Resource(
    private val resourceType: ResourceType,
    private val attributeMap: AttributeMap,
) {
    private var current: Int = initial()

    fun initial(): Int {
        return resourceType.initialAmount(attributeMap)
    }

    fun current(): Int {
        return current
    }

    fun maximum(): Int {
        return resourceType.maximumAmount(attributeMap)
    }

    fun set(value: Int): Boolean {
        current = value.coerceIn(0, maximum())
        return true
    }

    fun add(value: Int): Boolean {
        current += value
        current = current.coerceAtMost(maximum())
        return true // always success
    }

    fun take(value: Int): Boolean {
        if (value > current) return false // insufficient resource - return false
        current -= value
        current = current.coerceAtLeast(0) // minimum should be ZERO for all resource types
        return true
    }
}
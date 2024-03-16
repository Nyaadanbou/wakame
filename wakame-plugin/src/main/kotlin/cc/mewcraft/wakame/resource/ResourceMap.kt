package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.user.User

/**
 * Represents a ResourceMap owned by a subject.
 */
sealed interface ResourceMap {
    fun current(type: ResourceType): Int
    fun maximum(type: ResourceType): Int
    fun add(type: ResourceType, value: Int): Boolean
    fun take(type: ResourceType, value: Int): Boolean
}

/**
 * A Resource Map owned by a player.
 */
class PlayerResourceMap(
    private val user: User,
) : ResourceMap {
    private val data: Map<ResourceType, Resource> = buildMap {
        this[ResourceTypeRegistry.MANA] = Resource(user, ResourceTypeRegistry.MANA)
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

    private fun getResource(type: ResourceType): Resource {
        return requireNotNull(data[type]) { "The resource type $type" }
    }
}

/**
 * The object stores the amount of resource of a specific type.
 */
private class Resource(
    private val user: User,
    private val type: ResourceType,
) {
    private var current: Int = initial()

    fun initial(): Int {
        return type.initialAmount(user)
    }

    fun current(): Int {
        return current
    }

    fun maximum(): Int {
        return type.maximumAmount(user)
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

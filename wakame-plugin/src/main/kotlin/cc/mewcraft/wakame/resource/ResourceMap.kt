package cc.mewcraft.wakame.resource

import java.util.EnumMap

/**
 * Stores all types of resource states for a player.
 *
 * Not thread-safe.
 */
class ResourceMap internal constructor(
    private val supplier: ResourceSupplier,
) {
    private val resourceMap: EnumMap<ResourceType, Resource> = buildMap {
        /* FIXME Register new resource here */

        registerResource(ResourceType.MANA, supplier)

    }.let {
        EnumMap(it)
    }

    fun current(type: ResourceType): Int {
        return getResource(type).current()
    }

    fun maximum(type: ResourceType): Int {
        return getResource(type).maximum()
    }

    fun add(type: ResourceType, value: Int): Boolean {
        return getResource(type).add(value)
    }

    fun take(type: ResourceType, value: Int): Boolean {
        return getResource(type).take(value)
    }

    private fun getResource(type: ResourceType): Resource {
        return requireNotNull(resourceMap[type]) { "Cannot find resource type $type" }
    }

    private fun MutableMap<ResourceType, Resource>.registerResource(type: ResourceType, supplier: ResourceSupplier) {
        put(type, Resource(type, supplier))
    }
}

/**
 * Represents a resource of some type, such as stamina and mana.
 *
 * Not thread-safe!
 */
internal class Resource(
    private val type: ResourceType,
    private val supplier: ResourceSupplier,
) {
    private var current: Int = initial().coerceIn(0, maximum())

    fun initial(): Int {
        return supplier.initialValue(type)
    }

    fun current(): Int {
        return current
    }

    fun maximum(): Int {
        return supplier.maximumValue(type)
    }

    fun add(value: Int): Boolean {
        current += value
        current = current.coerceAtMost(maximum())
        return true // always success
    }

    fun take(value: Int): Boolean {
        if (value > current) return false // resource value never goes to negative
        current -= value
        current = current.coerceAtLeast(0) // minimum is ZERO for all resource types
        return true
    }
}

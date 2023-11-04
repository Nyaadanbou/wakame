package cc.mewcraft.wakame.attribute.handler

import java.util.*

enum class PlayerResourceType {
    MANA
}

/**
 * Stores a player's all type of resources.
 */
class PlayerResourceContainer {
    private val playerResourceMap: EnumMap<PlayerResourceType, PlayerResource> = EnumMap(
        mapOf(
            PlayerResourceType.MANA to PlayerResource(0, 100) // Mana
        )
    )

    fun current(type: PlayerResourceType): Int {
        return getResource(type).current()
    }

    fun maximum(type: PlayerResourceType): Int {
        return getResource(type).maximum()
    }

    fun incrementBy(type: PlayerResourceType, value: Int) {
        getResource(type).increment(value)
    }

    fun decrementBy(type: PlayerResourceType, value: Int) {
        getResource(type).decrement(value)
    }

    private fun getResource(type: PlayerResourceType): PlayerResource {
        return checkNotNull(playerResourceMap[type]) { "Cannot find resource type $type" }
    }
}

/**
 * Keeps the record of a player resource and provides basic read/write functions to this resource.
 */
private class PlayerResource(
    private var initial: Int,
    private var maximum: Int,
) {
    private var current: Int = initial.coerceIn(0, maximum)

    fun current(): Int {
        return current
    }

    fun maximum(): Int {
        return maximum
    }

    fun increment(value: Int) {
        current += value
        current = current.coerceAtMost(maximum)
    }

    fun decrement(value: Int) {
        current -= value
        current = current.coerceAtLeast(0) // Minimum is ZERO for all resource types
    }
}
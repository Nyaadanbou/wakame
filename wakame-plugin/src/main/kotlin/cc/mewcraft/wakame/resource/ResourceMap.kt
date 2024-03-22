package cc.mewcraft.wakame.resource

/**
 * Represents a ResourceMap owned by a subject.
 */
sealed interface ResourceMap {
    fun current(type: ResourceType): Int
    fun maximum(type: ResourceType): Int
    fun add(type: ResourceType, value: Int): Boolean
    fun take(type: ResourceType, value: Int): Boolean
}

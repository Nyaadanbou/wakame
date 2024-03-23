package cc.mewcraft.wakame.registry

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet

// Side note: use CMD-7 to navigate this file

/**
 * An abstract key-value registry.
 */
interface Registry<K, V> {
    /**
     * All the [instances][V] in this registry.
     */
    val objects: Set<V>

    /**
     * Gets specified value in this registry.
     *
     * @param uniqueId the name from which value you want to retrieve
     * @return the specified value or `null` if not existing
     */
    fun find(uniqueId: K?): V?

    /**
     * Gets specified value in this registry.
     *
     * @param uniqueId the name from which value you want to retrieve
     * @return the specified value
     * @throws IllegalStateException if the specified value does not exist
     */
    fun get(uniqueId: K): V =
        requireNotNull(find(uniqueId)) { "Can't find object for unique id: $uniqueId" }

    /**
     * Registers a new entry into this registry.
     *
     * @param uniqueId the unique identifier of the new entry
     * @param value the value to which the unique identifier maps
     */
    fun register(uniqueId: K, value: V)

    /**
     * Clears all registered entries.
     */
    fun clear()
}

/**
 * Operations of mapping [K] to [B], and the reversed way.
 */
interface BiRegistry<K, B> {
    /**
     * Gets binary identifier by unique identifier.
     */
    fun findBinaryIdBy(uniqueId: K?): B?

    /**
     * Gets binary identifier by unique identifier.
     */
    fun getBinaryIdBy(uniqueId: K): B

    /**
     * Gets unique identifier by binary identifier.
     */
    fun findUniqueIdBy(binaryId: B?): K?

    /**
     * Gets unique identifier by binary identifier.
     */
    fun getUniqueIdBy(binaryId: B): K

    /**
     * Registers a bi mapping.
     */
    fun register(uniqueId: K, binaryId: B)

    /**
     * Clears all registered bi entries.
     */
    fun clear()
}

/**
 * Operations of directly mapping binary identifier to object.
 *
 * Side note: the name of this interface is bad, never mind.
 */
@Suppress("PropertyName")
interface BiKnot<K, V, B> {
    val INSTANCES: Registry<K, V>
    val BI_LOOKUP: BiRegistry<K, B>

    /**
     * Gets an object by its binary identifier.
     *
     * @param binary the binary identifier
     * @return the specified object or `null`
     */
    fun findBy(binary: B): V? {
        return INSTANCES.find(BI_LOOKUP.findUniqueIdBy(binary))
    }

    /**
     * Gets an object by its binary identifier.
     *
     * @param binary the binary identifier
     * @return the specified element
     * @throws IllegalStateException if the object you look for does not exist
     */
    fun getBy(binary: B): V {
        return INSTANCES.get(BI_LOOKUP.getUniqueIdBy(binary))
    }
}

//<editor-fold desc="Internal Implementations">
internal class SimpleRegistry<K, V> : Registry<K, V> {
    private val uniqueId2ObjectMap: MutableMap<K, V> = LinkedHashMap() // order matters

    override val objects: Set<V>
        get() = ImmutableSet.copyOf(uniqueId2ObjectMap.values)

    override fun find(uniqueId: K?): V? {
        return if (uniqueId == null) null else uniqueId2ObjectMap[uniqueId]
    }

    override fun register(uniqueId: K, value: V) {
        uniqueId2ObjectMap[uniqueId] = value
    }

    override fun clear() {
        uniqueId2ObjectMap.clear()
    }
}

internal class SimpleBiRegistry<K, B> : BiRegistry<K, B> {
    private val uniqueId2BinaryIdMap: BiMap<K, B> = HashBiMap.create()

    override fun findBinaryIdBy(uniqueId: K?): B? {
        return if (uniqueId == null) null else uniqueId2BinaryIdMap[uniqueId]
    }

    override fun getBinaryIdBy(uniqueId: K): B {
        return requireNotNull(uniqueId2BinaryIdMap[uniqueId]) { "Can't find binary by unique id: $uniqueId" }
    }

    override fun findUniqueIdBy(binaryId: B?): K? {
        return if (binaryId == null) null else uniqueId2BinaryIdMap.inverse()[binaryId]
    }

    override fun getUniqueIdBy(binaryId: B): K {
        return requireNotNull(uniqueId2BinaryIdMap.inverse()[binaryId]) { "Can't find name by binary id: $binaryId" }
    }

    override fun register(uniqueId: K, binaryId: B) {
        uniqueId2BinaryIdMap[uniqueId] = binaryId
    }

    override fun clear() {
        uniqueId2BinaryIdMap.clear()
    }
}
//</editor-fold>

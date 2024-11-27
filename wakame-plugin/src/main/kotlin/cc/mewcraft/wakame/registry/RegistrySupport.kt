package cc.mewcraft.wakame.registry

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.jetbrains.annotations.TestOnly

// Side note: use CMD-7 to navigate this file

/**
 * An abstract key-value registry.
 */
sealed interface Registry<K, V> : Iterable<Map.Entry<K, V>> {
    /**
     * All the [instances][V] in this registry.
     */
    val values: Set<V>

    fun isEmpty(): Boolean

    /**
     * Gets specified value in this registry.
     *
     * @param uniqueId the name from which value you want to retrieve
     * @return the specified value or `null` if not existing
     */
    fun find(uniqueId: K?): V?

    /**
     * Checks whether the specific value is present in this registry.
     *
     * @param uniqueId the name to be checked
     * @return `true` if the specific value is present in this registry
     */
    fun has(uniqueId: K?): Boolean

    /**
     * Gets specified value in this registry.
     *
     * @param uniqueId the name from which value you want to retrieve
     * @return the specified value
     * @throws IllegalStateException if the specified value does not exist
     */
    operator fun get(uniqueId: K): V

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
    @TestOnly
    fun clear()
}

/**
 * 用于在忽略命名空间的前提下, 查询出所有具有相同路径的实例.
 *
 * 例如存在这些实例:
 * - `a:a`
 * - `b:a`
 * - `c:a`
 * 那么查询 [getFuzzy] 查询 `"a"` 就会返回这三个实例.
 */
class FuzzyRegistry<V> {
    private val path2Values: Object2ObjectOpenHashMap<String, ObjectArrayList<V>> = Object2ObjectOpenHashMap()

    fun register(path: String, value: V) {
        path2Values.getOrPut(path, ::ObjectArrayList).add(value)
    }

    fun getFuzzy(path: String): List<V> {
        return path2Values[path] ?: emptyList()
    }

    fun hasAny(path: String): Boolean {
        return getFuzzy(path).isNotEmpty()
    }

    fun clear() {
        path2Values.clear()
    }
}

/**
 * Operations of mapping [K] to [B], and the reversed way.
 */
sealed interface BiRegistry<K, B> {
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
 */
@Suppress("PropertyName")
sealed interface BiKnot<K, V, B> {
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
        return INSTANCES[BI_LOOKUP.getUniqueIdBy(binary)]
    }
}

//<editor-fold desc="Internal Implementations">
internal class SimpleRegistry<K, V> : Registry<K, V> {
    private val uniqueId2ObjectMap: MutableMap<K, V> = LinkedHashMap() // order matters

    override val values: Set<V>
        get() = ImmutableSet.copyOf(uniqueId2ObjectMap.values)

    override fun isEmpty(): Boolean {
        return uniqueId2ObjectMap.isEmpty()
    }

    override fun find(uniqueId: K?): V? {
        return if (uniqueId == null) null else uniqueId2ObjectMap[uniqueId]
    }

    override fun has(uniqueId: K?): Boolean {
        return find(uniqueId) != null
    }

    override operator fun get(uniqueId: K): V { // TODO 无脑抛异常有点太一刀切了，考虑返回空然后逐个处理
        return requireNotNull(find(uniqueId)) { "Can't find object by identifier `$uniqueId` in the registry" }
    }

    override fun register(uniqueId: K, value: V) {
        uniqueId2ObjectMap[uniqueId] = value
    }

    override fun clear() {
        uniqueId2ObjectMap.clear()
    }

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        return uniqueId2ObjectMap.iterator()
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

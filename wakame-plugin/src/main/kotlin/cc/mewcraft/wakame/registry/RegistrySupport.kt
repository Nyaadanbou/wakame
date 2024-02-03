package cc.mewcraft.wakame.registry

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet
import org.jetbrains.annotations.ApiStatus

// Side note: use CMD-7 to navigate this file

/**
 * An abstract registry.
 */
interface Registry<K, V> {
    @get:ApiStatus.Internal
    val name2ObjectMapping: MutableMap<K, V>

    /**
     * All the elements in this registry.
     */
    val all: Set<V>

    /**
     * Gets specified element in this registry.
     *
     * @param name the name from which element you want to retrieve
     * @return the specified element or `null` if not existing
     */
    fun get(name: K?): V?

    /**
     * Gets specified element in this registry.
     *
     * @param name the name from which element you want to retrieve
     * @return the specified element
     * @throws IllegalStateException if the specified element does not exist
     */
    fun getOrThrow(name: K): V =
        checkNotNull(get(name)) { "Can't find object for name $name" }

    /**
     * Registers a new element into this registry.
     *
     * @param name the name of the new element
     * @param value the new element itself
     */
    fun registerName2Object(name: K, value: V)
}

internal class RegistryBase<K, V> : Registry<K, V> {
    override val name2ObjectMapping: MutableMap<K, V> = HashMap()

    override val all: Set<V>
        get() = ImmutableSet.copyOf(name2ObjectMapping.values)

    override fun get(name: K?): V? {
        return if (name == null) null else name2ObjectMapping[name]
    }

    override fun registerName2Object(name: K, value: V) {
        name2ObjectMapping[name] = value
    }
}

/**
 * Operations of mapping [STRING] to [BINARY], and the reversed way.
 */
interface BiMapRegistry<STRING, BINARY> {
    @get:ApiStatus.Internal
    val binary2NameMapping: BiMap<STRING, BINARY>

    fun getBinaryBy(name: STRING?): BINARY?
    fun getBinaryByOrThrow(name: STRING): BINARY
    fun getNameBy(binary: BINARY?): STRING?
    fun getNameByOrThrow(binary: BINARY): STRING
    fun registerBinary2Name(name: STRING, binary: BINARY)
}

internal class BiMapRegistryBase<STRING, BINARY> : BiMapRegistry<STRING, BINARY> {
    override val binary2NameMapping: BiMap<STRING, BINARY> = HashBiMap.create()

    override fun getBinaryBy(name: STRING?): BINARY? =
        if (name == null) null else binary2NameMapping[name]

    override fun getBinaryByOrThrow(name: STRING): BINARY =
        checkNotNull(binary2NameMapping[name]) { "Can't find binary by name $name" }

    override fun getNameBy(binary: BINARY?): STRING? =
        if (binary == null) null else binary2NameMapping.inverse()[binary]

    override fun getNameByOrThrow(binary: BINARY): STRING =
        checkNotNull(binary2NameMapping.inverse()[binary]) { "Can't find name by binary $binary" }

    override fun registerBinary2Name(name: STRING, binary: BINARY) {
        binary2NameMapping[name] = binary
    }
}

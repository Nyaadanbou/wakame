package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.annotation.InternalApi
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet

// Side note: use CMD-7 to navigate this file

/**
 * An abstract registry.
 */
interface Registry<K, V> {
    @InternalApi
    val name2ObjectMapping: MutableMap<K, V>

    /**
     * All the values in this registry.
     */
    val values: Set<V>

    /**
     * Gets specified value in this registry.
     *
     * @param name the name from which value you want to retrieve
     * @return the specified value or `null` if not existing
     */
    fun get(name: K?): V?

    /**
     * Gets specified value in this registry.
     *
     * @param name the name from which value you want to retrieve
     * @return the specified value
     * @throws IllegalStateException if the specified value does not exist
     */
    fun getOrThrow(name: K): V =
        requireNotNull(get(name)) { "Can't find object for name $name" }

    /**
     * Registers a new entry into this registry.
     *
     * @param name the name of the new entry
     * @param value the new value
     */
    fun registerName2Object(name: K, value: V)

    /**
     * Clears all entries.
     */
    fun clearName2Object()
}

/**
 * Operations of mapping [STRING] to [BINARY], and the reversed way.
 */
interface BiMapRegistry<STRING, BINARY> {
    @InternalApi
    val binary2NameMapping: BiMap<STRING, BINARY>

    fun getBinaryBy(name: STRING?): BINARY?
    fun getBinaryByOrThrow(name: STRING): BINARY
    fun getNameBy(binary: BINARY?): STRING?
    fun getNameByOrThrow(binary: BINARY): STRING
    fun registerBinary2Name(name: STRING, binary: BINARY)

    /**
     * Clears all entries.
     */
    fun clearBinary2Name()
}

//<editor-fold desc="Internal Implementations">
@OptIn(InternalApi::class)
internal class HashMapRegistry<K, V> : Registry<K, V> {
    override val name2ObjectMapping: MutableMap<K, V> = LinkedHashMap() // order matters

    override val values: Set<V>
        get() = ImmutableSet.copyOf(name2ObjectMapping.values)

    override fun get(name: K?): V? {
        return if (name == null) null else name2ObjectMapping[name]
    }

    override fun registerName2Object(name: K, value: V) {
        name2ObjectMapping[name] = value
    }

    override fun clearName2Object() {
        name2ObjectMapping.clear()
    }
}

@OptIn(InternalApi::class)
internal class HashBiMapRegistry<STRING, BINARY> : BiMapRegistry<STRING, BINARY> {
    override val binary2NameMapping: BiMap<STRING, BINARY> = HashBiMap.create()

    override fun getBinaryBy(name: STRING?): BINARY? =
        if (name == null) null else binary2NameMapping[name]

    override fun getBinaryByOrThrow(name: STRING): BINARY =
        requireNotNull(binary2NameMapping[name]) { "Can't find binary by name $name" }

    override fun getNameBy(binary: BINARY?): STRING? =
        if (binary == null) null else binary2NameMapping.inverse()[binary]

    override fun getNameByOrThrow(binary: BINARY): STRING =
        requireNotNull(binary2NameMapping.inverse()[binary]) { "Can't find name by binary $binary" }

    override fun registerBinary2Name(name: STRING, binary: BINARY) {
        binary2NameMapping[name] = binary
    }

    override fun clearBinary2Name() {
        binary2NameMapping.clear()
    }
}
//</editor-fold>

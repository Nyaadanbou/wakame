package cc.mewcraft.wakame.util

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Utility class for looking up enum instances by name or ordinal.
 */
object EnumLookup {

    // Cache for name lookups
    private val NAME_CACHE = WeakHashMap<Class<*>, Map<String, Enum<*>>>()
    // Cache for ordinal lookups
    private val ORDINAL_CACHE = WeakHashMap<Class<*>, Map<Int, Enum<*>>>()

    /**
     * Look up an enum instance by name using reified type parameter.
     *
     * @param name the name of the enum constant
     * @return a Result containing the enum instance if found, or an exception if not
     */
    inline fun <reified E : Enum<E>> lookupByName(name: String): Result<E> =
        runCatching {
            enumValueOf<E>(name.trim().uppercase().replace('-', '_').replace(' ', '_'))
        }

    /**
     * Look up an enum instance by name with a default value using reified type parameter.
     *
     * @param name the name of the enum constant
     * @param defaultValue the default value to return if the lookup fails
     * @return the enum instance if found, or the default value
     */
    inline fun <reified E : Enum<E>> lookupByName(name: String, defaultValue: E): E =
        lookupByName<E>(name).getOrElse { defaultValue }

    /**
     * Look up an enum instance by ordinal using reified type parameter.
     *
     * @param ordinal the ordinal value of the enum constant
     * @return a Result containing the enum instance if found, or an exception if not
     */
    inline fun <reified E : Enum<E>> lookupByOrdinal(ordinal: Int): Result<E> =
        runCatching {
            val values = enumValues<E>()
            if (ordinal in values.indices) values[ordinal]
            else throw IllegalArgumentException("Invalid ordinal $ordinal for enum ${E::class.java.name}")
        }

    /**
     * Look up an enum instance by ordinal with a default value using reified type parameter.
     *
     * @param ordinal the ordinal value of the enum constant
     * @param defaultValue the default value to return if the lookup fails
     * @return the enum instance if found, or the default value
     */
    inline fun <reified E : Enum<E>> lookupByOrdinal(ordinal: Int, defaultValue: E): E =
        lookupByOrdinal<E>(ordinal).getOrElse { defaultValue }

    /**
     * Look up an enum instance by name using Class parameter.
     *
     * @param enumClass the enum class
     * @param name the name of the enum constant
     * @return the enum instance if found, or null if not
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Enum<E>> lookupByName(enumClass: Class<E>, name: String): E? {
        if (name.isBlank()) {
            return null
        }

        val normalizedName = name.trim().uppercase().replace('-', '_').replace(' ', '_')
        val vals = NAME_CACHE.computeIfAbsent(enumClass) { cls ->
            val map = ConcurrentHashMap<String, Enum<*>>()
            (cls.enumConstants as Array<Enum<*>>).forEach { enum ->
                map[enum.name] = enum
            }
            Collections.unmodifiableMap(map)
        }

        return vals[normalizedName] as E?
    }

    /**
     * Look up an enum instance by ordinal using Class parameter.
     *
     * @param enumClass the enum class
     * @param ordinal the ordinal value of the enum constant
     * @return the enum instance if found, or null if not
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Enum<E>> lookupByOrdinal(enumClass: Class<E>, ordinal: Int): E? {
        if (ordinal < 0) {
            return null
        }

        val vals = ORDINAL_CACHE.computeIfAbsent(enumClass) { cls ->
            val map = ConcurrentHashMap<Int, Enum<*>>()
            (cls.enumConstants as Array<Enum<*>>).forEach { enum ->
                map[enum.ordinal] = enum
            }
            Collections.unmodifiableMap(map)
        }

        return vals[ordinal] as E?
    }

    // Legacy methods for backward compatibility

    inline fun <reified E : Enum<E>> lookup(name: String): Result<E> = lookupByName<E>(name)
    inline fun <reified E : Enum<E>> lookup(name: String, defaultValue: E): E = lookupByName(name, defaultValue)
}
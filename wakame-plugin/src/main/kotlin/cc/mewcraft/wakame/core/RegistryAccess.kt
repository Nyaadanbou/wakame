package cc.mewcraft.wakame.core

interface RegistryAccess {
    companion object {
        @JvmField
        val EMPTY = ImmutableRegistryAccess(emptyMap())
    }

    fun <E> registry(key: ResourceKey<out Registry<E>>): Registry<E>?
    fun <E> registryOrThrow(key: ResourceKey<out Registry<E>>): Registry<E> {
        return registry(key) ?: throw IllegalStateException("Missing registry: $key")
    }

    fun registries(): Sequence<RegistryEntry<*>>
}

data class RegistryEntry<T>(val key: ResourceKey<out Registry<T>>, val value: Registry<T>) {
    companion object {
        fun <T, R : Registry<out T>> fromMapEntry(entry: Map.Entry<ResourceKey<out Registry<*>>, R>): RegistryEntry<T> {
            return fromUntyped(entry.key, entry.value)
        }

        fun <T> fromUntyped(key: ResourceKey<out Registry<*>>, value: Registry<*>): RegistryEntry<T> {
            @Suppress("UNCHECKED_CAST")
            return RegistryEntry(key as ResourceKey<out Registry<T>>, value as Registry<T>)
        }
    }
}

class ImmutableRegistryAccess(registries: Map<out ResourceKey<out Registry<*>>, Registry<*>>) : RegistryAccess {
    private val registries: Map<ResourceKey<out Registry<*>>, Registry<*>> = HashMap(registries)

    constructor(registries: List<Registry<*>>) : this(registries.associateBy(Registry<*>::key))
    constructor(registries: Sequence<RegistryEntry<*>>) : this(registries.associateBy(RegistryEntry<*>::key, RegistryEntry<*>::value))

    override fun <T> registry(key: ResourceKey<out Registry<T>>): Registry<T>? {
        @Suppress("UNCHECKED_CAST")
        return registries[key] as Registry<T>?
    }

    override fun registries(): Sequence<RegistryEntry<*>> {
        return registries.entries.asSequence().map { RegistryEntry.fromMapEntry(it) }
    }
}
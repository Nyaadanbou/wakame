package cc.mewcraft.wakame.core

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap

class MutableRegistryAccess : RegistryAccess {
    private val registries: MutableMap<ResourceKey<out Registry<*>>, Registry<*>> = Reference2ObjectOpenHashMap(64)

    fun <E> register(key: ResourceKey<out Registry<E>>, registry: WritableRegistry<E>): WritableRegistry<E> {
        registries[key] = registry
        return registry
    }

    /**
     * 重置注册表的所有状态.
     */
    fun resetRegistries() {
        registries.forEach { (_, registry) ->
            if (registry is WritableRegistry<*>) {
                registry.resetRegistry()
            }
        }
    }

    override fun <E> registry(key: ResourceKey<out Registry<E>>): Registry<E>? {
        @Suppress("UNCHECKED_CAST")
        return registries[key] as Registry<E>?
    }

    override fun registries(): Sequence<RegistryEntry<*>> {
        return registries.entries.asSequence().map { RegistryEntry.fromMapEntry(it) }
    }
}

class ImmutableRegistryAccess(registries: Map<out ResourceKey<out Registry<*>>, Registry<*>>) : RegistryAccess {
    private val registries: Map<ResourceKey<out Registry<*>>, Registry<*>> = Reference2ObjectOpenHashMap(registries)

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
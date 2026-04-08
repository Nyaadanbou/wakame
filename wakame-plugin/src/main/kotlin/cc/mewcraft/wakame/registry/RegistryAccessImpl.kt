package cc.mewcraft.wakame.registry

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap

class MutableRegistryAccess : RegistryAccess {
    private val registries: MutableMap<RegistryKey<out Registry<*>>, Registry<*>> = Reference2ObjectOpenHashMap(64)

    fun <E> add(key: RegistryKey<out Registry<E>>, registry: WritableRegistry<E>): WritableRegistry<E> {
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

    override fun <E> get(key: RegistryKey<out Registry<E>>): Registry<E>? {
        @Suppress("UNCHECKED_CAST")
        return registries[key] as Registry<E>?
    }

    override fun sequenceAllRegistries(): Sequence<RegistryAccess.Entry<*>> {
        return registries.entries.asSequence().map { RegistryAccess.Entry.of(it) }
    }
}

class ImmutableRegistryAccess(registries: Map<out RegistryKey<out Registry<*>>, Registry<*>>) : RegistryAccess {
    private val registries: Map<RegistryKey<out Registry<*>>, Registry<*>> = Reference2ObjectOpenHashMap(registries)

    constructor(registries: List<Registry<*>>) : this(registries.associateBy(Registry<*>::key))
    constructor(registries: Sequence<RegistryAccess.Entry<*>>) : this(registries.associateBy(RegistryAccess.Entry<*>::key, RegistryAccess.Entry<*>::value))

    override fun <T> get(key: RegistryKey<out Registry<T>>): Registry<T>? {
        @Suppress("UNCHECKED_CAST")
        return registries[key] as Registry<T>?
    }

    override fun sequenceAllRegistries(): Sequence<RegistryAccess.Entry<*>> {
        return registries.entries.asSequence().map { RegistryAccess.Entry.of(it) }
    }
}
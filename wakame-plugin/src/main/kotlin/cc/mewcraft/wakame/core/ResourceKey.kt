package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.registries.KoishRegistryKeys
import com.google.common.collect.MapMaker
import java.util.concurrent.ConcurrentMap

@ConsistentCopyVisibility
data class ResourceKey<T>
private constructor(
    val registryName: ResourceLocation,
    val location: ResourceLocation,
) {
    companion object {
        // 确保值的全局唯一性
        private val VALUES: ConcurrentMap<InternKey, ResourceKey<*>> = MapMaker().weakValues().makeMap()

        fun <T> create(registry: ResourceLocation, value: ResourceLocation): ResourceKey<T> {
            @Suppress("UNCHECKED_CAST")
            return VALUES.computeIfAbsent(InternKey(registry, value)) { pair -> ResourceKey<T>(pair.registry, pair.location) } as ResourceKey<T>
        }

        fun <T> create(registry: ResourceKey<in Registry<T>>, value: ResourceLocation): ResourceKey<T> {
            return create(registry.location, value)
        }

        fun <T> createRegistryKey(registry: ResourceLocation): ResourceKey<T> {
            return create(KoishRegistryKeys.ROOT_REGISTRY_NAME, registry)
        }
    }

    fun isFor(registry: ResourceKey<*>): Boolean {
        return registryName == registry.location
    }

    fun <E> cast(registryRef: ResourceKey<in Registry<E>>): ResourceKey<E>? {
        return if (isFor(registryRef)) {
            @Suppress("UNCHECKED_CAST")
            this as ResourceKey<E>
        } else null
    }

    override fun toString(): String {
        return "ResourceKey[$registryName / $location]"
    }

    private data class InternKey(val registry: ResourceLocation, val location: ResourceLocation)
}
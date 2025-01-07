package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.registries.KoishRegistryKeys
import com.google.common.collect.MapMaker
import java.util.concurrent.ConcurrentMap

/**
 * 代表一个资源的键名, 包含一个 [registryId] 用于确定该资源所在的 [Registry].
 */
class ResourceKey<T>
private constructor(
    val registryId: ResourceLocation, // e.g. "koish:item", "koish:element"
    val location: ResourceLocation, // depends on the entries in registry
) {
    companion object {
        // 确保值的全局唯一性
        private val VALUES: ConcurrentMap<InternKey, ResourceKey<*>> = MapMaker().weakValues().makeMap()

        fun <T> create(registryName: ResourceLocation, value: ResourceLocation): ResourceKey<T> {
            @Suppress("UNCHECKED_CAST")
            return VALUES.computeIfAbsent(InternKey(registryName, value)) { pair -> ResourceKey<T>(pair.registry, pair.location) } as ResourceKey<T>
        }

        fun <T> create(registryKey: ResourceKey<out Registry<T>>, value: ResourceLocation): ResourceKey<T> {
            return create(registryKey.location, value)
        }

        // 创建的 ResourceKey 位于 koish:root
        fun <T> createRegistryKey(registryName: ResourceLocation): ResourceKey<T> {
            return create(KoishRegistryKeys.ROOT_REGISTRY_NAME, registryName)
        }
    }

    fun isFor(registryKey: ResourceKey<*>): Boolean {
        return registryId == registryKey.location
    }

    fun <E> safeCast(registryRef: ResourceKey<in Registry<E>>): ResourceKey<E>? {
        return if (isFor(registryRef)) {
            @Suppress("UNCHECKED_CAST")
            this as ResourceKey<E>
        } else null
    }

    // 返回的 ResourceKey 位于 koish:root
    fun registryKey(): ResourceKey<T> {
        return createRegistryKey<T>(registryId)
    }

    override fun toString(): String {
        return "ResourceKey[$registryId / $location]"
    }

    private data class InternKey(val registry: ResourceLocation, val location: ResourceLocation)
}
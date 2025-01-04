package cc.mewcraft.wakame.core

/**
 * 提供访问注册表的方式.
 *
 * 如果程序员只是想访问注册表里的数据, 应该直接使用已经创建好的单例.
 */
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

package cc.mewcraft.wakame.registry

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

    fun <E> get(key: RegistryKey<out Registry<E>>): Registry<E>?
    fun <E> getOrThrow(key: RegistryKey<out Registry<E>>): Registry<E> {
        return get(key) ?: throw IllegalStateException("Missing registry: $key")
    }

    fun sequenceAllRegistries(): Sequence<Entry<*>>

    data class Entry<T>(val key: RegistryKey<out Registry<T>>, val value: Registry<T>) {
        companion object {
            fun <T, R : Registry<out T>> of(entry: Map.Entry<RegistryKey<out Registry<*>>, R>): Entry<T> {
                return of(entry.key, entry.value)
            }

            fun <T> of(key: RegistryKey<out Registry<*>>, value: Registry<*>): Entry<T> {
                @Suppress("UNCHECKED_CAST")
                return Entry(key as RegistryKey<out Registry<T>>, value as Registry<T>)
            }
        }
    }
}

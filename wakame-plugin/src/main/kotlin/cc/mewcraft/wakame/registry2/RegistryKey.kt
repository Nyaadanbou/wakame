package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.util.Identifier
import com.google.common.collect.MapMaker
import java.util.concurrent.ConcurrentMap

/**
 * 代表一个资源的键名, 包含两种信息:
 * - [registry] 描述了该资源所在的注册表 [Registry]
 * - [value] 描述了该资源在 [Registry] 中的位置
 *
 * 因此, 该键名可用于从任意注册表中确定一个数据.
 *
 * ### 对象池
 * [RegistryKey] 采用了对象池的设计. 相同的 [registry]
 * 和 [value] 会共享一个 [RegistryKey] 实例.
 * 因此可以安全使用 `===` 比较不同的实例.
 */
class RegistryKey<T>
private constructor(
    val registry: Identifier, // e.g. "koish:item", "koish:element"
    val value: Identifier, // depends on the entries in registry
) {
    companion object {
        // 确保值的全局唯一性
        private val INSTANCES: ConcurrentMap<InternKey, RegistryKey<*>> = MapMaker().weakValues().makeMap()

        fun <T> of(registryName: Identifier, value: Identifier): RegistryKey<T> {
            @Suppress("UNCHECKED_CAST")
            return INSTANCES.computeIfAbsent(InternKey(registryName, value)) { pair -> RegistryKey<T>(pair.registry, pair.value) } as RegistryKey<T>
        }

        fun <T> of(registryKey: RegistryKey<out Registry<T>>, value: Identifier): RegistryKey<T> {
            return of(registryKey.value, value)
        }

        // 创建的 ResourceKey 位于 koish:root
        fun <T> ofRegistry(registryName: Identifier): RegistryKey<T> {
            return of(KoishRegistryKeys.ROOT_REGISTRY_NAME, registryName)
        }
    }

    fun isOf(registryKey: RegistryKey<*>): Boolean {
        return registry == registryKey.value
    }

    fun <E> tryCast(registryRef: RegistryKey<in Registry<E>>): RegistryKey<E>? {
        return if (isOf(registryRef)) {
            @Suppress("UNCHECKED_CAST")
            this as RegistryKey<E>
        } else null
    }

    // 返回的 ResourceKey 位于 koish:root
    fun registryRef(): RegistryKey<T> {
        return ofRegistry<T>(registry)
    }

    fun toPrettyString(): String {
        return "[${registry} / ${value}]"
    }

    override fun toString(): String {
        return "RegistryKey[$registry / ${value}]"
    }

    private data class InternKey(val registry: Identifier, val value: Identifier)
}
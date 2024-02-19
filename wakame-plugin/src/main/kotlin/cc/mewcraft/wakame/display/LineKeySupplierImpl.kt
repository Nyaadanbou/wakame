package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.elementOrNull
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.scheme.meta.SchemeMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeMetaKeys
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import kotlin.reflect.KClass

// TODO 统一 key 的生成实现

internal class AbilityLineKeySupplierImpl : AbilityLineKeySupplier {
    override fun get(obj: BinaryAbilityCore): FullKey {
        // 技能在 NBT 中的 key 就是它的 full key
        return obj.key
    }
}

internal class AttributeLineKeySupplierImpl : AttributeLineKeySupplier {
    /**
     * Full Keys in this map are double-indexed: `key` + `operation`.
     */
    private val cachedFullKeys: Object2ObjectOpenHashMap<Key, Reference2ObjectOpenHashMap<AttributeModifier.Operation, FullKey>> = Object2ObjectOpenHashMap()

    /**
     * Full Keys in this map are triple-indexed: `key` + `operation` + `element`.
     */
    private val cachedFullKeysWithElement: Object2ObjectOpenHashMap<Key, Reference2ObjectOpenHashMap<AttributeModifier.Operation, Reference2ObjectOpenHashMap<Element, FullKey>>> = Object2ObjectOpenHashMap()

    /**
     * Caches a full key from the given triple indexes.
     */
    private fun put(key: Key, operation: AttributeModifier.Operation, element: Element? = null, fullKey: FullKey) {
        if (element == null) {
            cachedFullKeys
                .getOrPut(key) { Reference2ObjectOpenHashMap(4, 0.9f) } // 运算模式最多3个
                .put(operation, fullKey)
        } else {
            cachedFullKeysWithElement
                .getOrPut(key) { Reference2ObjectOpenHashMap(4, 0.9f) }
                .getOrPut(operation) { Reference2ObjectOpenHashMap(8, 0.9f) } // 元素一般最多8个
                .put(element, fullKey)
        }
    }

    /**
     * Gets a full key from the given triple indexes.
     */
    private fun get(key: Key, operation: AttributeModifier.Operation, element: Element? = null): FullKey? {
        return if (element == null) {
            cachedFullKeys[key]?.get(operation)
        } else {
            cachedFullKeysWithElement[key]?.get(operation)?.get(element)
        }
    }

    /**
     * Returns the full key for the given triple indexes ([key], [operation], [element]) if the full key is cached and not `null`.
     * Otherwise, calls the [defaultValue] function, puts its result into the cache under the given indexes and returns the call result.
     *
     * Note that the operation is not guaranteed to be atomic if the map is being modified concurrently.
     */
    private inline fun getOrPut(key: Key, operation: AttributeModifier.Operation, element: Element? = null, defaultValue: () -> Key): FullKey {
        val value = get(key, operation, element)
        return if (value == null) {
            val answer = defaultValue()
            put(key, operation, element, answer)
            answer
        } else {
            value
        }
    }

    /**
     * Removes the cache from the given triple indexes.
     *
     * @return the old cache
     */
    private fun remove(key: Key, operation: AttributeModifier.Operation, element: Element? = null): FullKey? {
        if (element == null) {
            val map1 = cachedFullKeys[key] ?: return null
            val value = map1.remove(operation)
            if (map1.isEmpty()) {
                cachedFullKeys.remove(key)
            }
            return value
        } else {
            val map1 = cachedFullKeysWithElement[key] ?: return null
            val map2 = map1[operation] ?: return null
            val value = map2[element]
            if (map2.isEmpty()) {
                map1.remove(operation)
                if (map1.isEmpty()) {
                    cachedFullKeysWithElement.remove(key)
                }
            }
            return value
        }
    }

    override fun get(obj: BinaryAttributeCore): FullKey {
        // 属性的 full key 根据 id + operation + element 共同决定
        // 属性的 full key 格式目前只有两种
        // 1. attribute:_id_/_operation
        // 2. attribute:_id_/_operation_/_element_

        val key = obj.key
        val operation = obj.value.operation
        val element = obj.value.elementOrNull
        val fullKey = getOrPut(key, operation, element) {
            val newValue = buildString {
                append(key.value())
                append(".")
                append(operation.key)
                element?.let {
                    append(".")
                    append(it.key)
                }
            }
            Key.key(key.namespace(), newValue)
        }
        return fullKey
    }
}

internal class MetaLineKeySupplierImpl : MetaLineKeySupplier {
    override fun get(obj: KClass<out SchemeMeta<*>>): FullKey {
        // 模板元数据的 key 就是它的 full key
        return SchemeMetaKeys.get(obj)
    }
}

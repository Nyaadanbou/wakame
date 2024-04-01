package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.element
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.util.Key
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key

internal class AbilityKeySupplierImpl(
    private val config: RendererConfiguration,
) : AbilityKeySupplier {
    override fun get(obj: BinaryAbilityCore): FullKey {
        val fullKey = obj.key // 技能的 Full Key 就是它在 NBT 中的 Key
        val rawKey = fullKey // 技能的 Raw Key 跟它的 Full Key 一致
        return if (rawKey !in config.rawKeys) {
            SKIP_RENDERING
        } else fullKey
    }
}

// to clarify the table indexes
private typealias AttributeKeyTable1<K1, K2, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, V>>
private typealias AttributeKeyTable2<K1, K2, K3, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, Reference2ObjectOpenHashMap<K3, V>>>

internal class AttributeKeySupplierImpl(
    private val config: RendererConfiguration,
) : AttributeKeySupplier {
    //<editor-fold desc="Implementation of a Map indexed by triple keys">
    /**
     * Full Keys in this map are double-indexed: `key` + `operation`.
     */
    private val cachedFullKeys: AttributeKeyTable1<Key, Operation, FullKey> = AttributeKeyTable1()

    /**
     * Full Keys in this map are triple-indexed: `key` + `operation` +
     * `element`.
     */
    private val cachedFullKeysWithElement: AttributeKeyTable2<Key, Operation, Element, FullKey> = AttributeKeyTable2()

    /**
     * Caches a full key from the given triple indexes.
     */
    @Suppress("ReplacePutWithAssignment")
    private fun put(key: RawKey, operation: Operation, element: Element? = null, fullKey: FullKey) {
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
    private fun get(key: RawKey, operation: Operation, element: Element? = null): FullKey? {
        return if (element == null) {
            cachedFullKeys[key]?.get(operation)
        } else {
            cachedFullKeysWithElement[key]?.get(operation)?.get(element)
        }
    }

    /**
     * Returns the full key for the given triple indexes ([rawKey], [operation],
     * [element]) if the full key is cached and not `null`. Otherwise, calls
     * the [defaultValue] function, puts its result into the cache under the
     * given indexes and returns the call result.
     *
     * Note that the operation is not guaranteed to be atomic if the map is
     * being modified concurrently.
     */
    private inline fun getOrPut(
        rawKey: RawKey,
        operation: Operation,
        element: Element? = null,
        defaultValue: () -> FullKey,
    ): FullKey {
        val value = get(rawKey, operation, element)
        return if (value == null) {
            val answer = defaultValue()
            put(rawKey, operation, element, answer)
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
    private fun remove(key: RawKey, operation: Operation, element: Element? = null): FullKey? {
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
    //</editor-fold>

    override fun get(obj: BinaryAttributeCore): FullKey {
        // 属性的 Full Key 目前有两种
        //   attribute:_id_._operation             <- 由 运算模式 衍生
        //   attribute:_id_._operation_._element_  <- 由 运算模式 & 元素种类 衍生

        val rawKey = obj.key
        if (rawKey !in config.rawKeys) {
            return SKIP_RENDERING
        }
        val operation = obj.data.operation
        val element = obj.data.element
        val fullKey = getOrPut(rawKey, operation, element) {
            val newValue = buildString {
                append(rawKey.value())
                append(".")
                append(operation.key)
                element?.let {
                    append(".")
                    append(it.uniqueId)
                }
            }
            Key(rawKey.namespace(), newValue)
        }
        return fullKey
    }
}

internal class ItemMetaKeySupplierImpl(
    private val config: RendererConfiguration,
) : ItemMetaKeySupplier {
    override fun get(obj: BinaryItemMeta<*>): FullKey {
        val fullKey = obj.key // 元数据的 Full Key 就是它的 Key
        val rawKey = fullKey // 元数据的 Raw Key 跟它的 Full Key 一致
        return if (rawKey !in config.rawKeys) {
            SKIP_RENDERING
        } else fullKey
    }
}

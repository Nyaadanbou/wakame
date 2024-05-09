package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.hasComponent
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.StringCombiner
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

internal data class AttributeLoreLine(
    override val key: FullKey,
    override val lines: List<Component>,
) : LoreLine

internal data class AttributeLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
    private val derivation: Derivation,
) : DynamicLoreMeta {
    /**
     * 根据以下衍生规则:
     * - attribute:{id}:{operation}           <-- 第一种
     * - attribute:{id}:{operation}:{element} <-- 第二种
     *
     * 为该属性生成所有的 full keys
     */
    override val fullKeys: List<FullKey>
        get() {
            if (rawKey == AttributeRegistry.EMPTY_KEY) {
                return listOf(rawKey) // for `empty`, do not derive
            }

            val namespace = rawKey.namespace()
            val values = StringCombiner(rawKey.value(), ".") {
                addList(derivation.operationIndex)
                addList(derivation.elementIndex, AttributeRegistry.FACADES[rawKey].attributeComponentMetadata.hasComponent<AttributeComponent.Element<*>>())
            }.combine()

            return values.map { Key(namespace, it) }
        }

    class Derivation(
        operationIndex: Provider<List<String>>,
        elementIndex: Provider<List<String>>,
    ) : Examinable {
        /**
         * 运算模式的顺序。
         */
        val operationIndex: List<String> by operationIndex

        /**
         * 元素种类的顺序。
         */
        val elementIndex: List<String> by elementIndex

        init {
            // validate values
            this.operationIndex.forEach { Operation.byKey(it) }
            this.elementIndex.forEach { ElementRegistry.INSTANCES[it] }
        }

        override fun toString(): String {
            return toSimpleString()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("operationIndex", operationIndex),
                ExaminableProperty.of("elementIndex", elementIndex),
            )
        }
    }
}

internal class AttributeLineKeyFactory(
    private val config: RendererConfiguration,
) : LineKeyFactory<BinaryAttributeCore> {
    override fun get(obj: BinaryAttributeCore): FullKey {
        // 属性的 full key 目前有两种
        //   attribute:{facade_id}.{operation}
        //   attribute:{facade_id}.{operation}.{element}

        val rawKey = obj.key
        if (rawKey !in config.rawKeys) {
            return LineKeyFactory.SKIP_DISPLAY
        }

        val operation = obj.operation
        val element = obj.elementOrNull
        val fullKey = getOrPut(rawKey, operation, element) {
            Key(rawKey.namespace(), buildString {
                // append raw key
                append(rawKey.value())

                // append operation
                append("."); append(operation.key)

                // append element if it presents
                element?.let { append("."); append(it.uniqueId) }
            })
        }

        return fullKey
    }

    /**
     * This companion object serves as a singleton Map indexed by double or triple keys.
     */
    private companion object Index {
        /**
         * The full keys in this map are double-indexed by `key` + `operation`.
         */
        val INDEX_FOR_COMBINATION_1: AttributeDoubleKeyTable<Key, Operation, FullKey> by ReloadableProperty { AttributeDoubleKeyTable() }

        /**
         * The full keys in this map are triple-indexed by `key` + `operation` + `element`.
         */
        val INDEX_FOR_COMBINATION_2: AttributeTripleKeyTable<Key, Operation, Element, FullKey> by ReloadableProperty { AttributeTripleKeyTable() }

        /**
         * Caches a full key from the given triple indexes.
         */
        fun put(key: RawKey, operation: Operation, element: Element?, fullKey: FullKey) {
            if (element == null) {
                INDEX_FOR_COMBINATION_1
                    .getOrPut(key) { Reference2ObjectOpenHashMap(4, 0.9f) } // 运算模式最多3个
                    .put(operation, fullKey)
            } else {
                INDEX_FOR_COMBINATION_2
                    .getOrPut(key) { Reference2ObjectOpenHashMap(4, 0.9f) }
                    .getOrPut(operation) { Reference2ObjectOpenHashMap(8, 0.9f) } // 元素一般最多8个
                    .put(element, fullKey)
            }
        }

        /**
         * Gets a full key from the given triple indexes.
         */
        fun get(key: RawKey, operation: Operation, element: Element?): FullKey? {
            return if (element == null) {
                INDEX_FOR_COMBINATION_1[key]?.get(operation)
            } else {
                INDEX_FOR_COMBINATION_2[key]?.get(operation)?.get(element)
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
        inline fun getOrPut(
            rawKey: RawKey,
            operation: Operation,
            element: Element?,
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
        fun remove(key: RawKey, operation: Operation, element: Element? = null): FullKey? {
            if (element == null) {
                val map1 = INDEX_FOR_COMBINATION_1[key] ?: return null
                val value = map1.remove(operation)
                if (map1.isEmpty()) {
                    INDEX_FOR_COMBINATION_1.remove(key)
                }
                return value
            } else {
                val map1 = INDEX_FOR_COMBINATION_2[key] ?: return null
                val map2 = map1[operation] ?: return null
                val value = map2[element]
                if (map2.isEmpty()) {
                    map1.remove(operation)
                    if (map1.isEmpty()) {
                        INDEX_FOR_COMBINATION_2.remove(key)
                    }
                }
                return value
            }
        }
    }
}

private typealias AttributeDoubleKeyTable<K1, K2, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, V>>
private typealias AttributeTripleKeyTable<K1, K2, K3, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, Reference2ObjectOpenHashMap<K3, V>>>

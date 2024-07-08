package cc.mewcraft.wakame.item.components.cells.cores.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.DisplaySupport
import cc.mewcraft.wakame.display.DynamicLoreMeta
import cc.mewcraft.wakame.display.DynamicLoreMetaCreator
import cc.mewcraft.wakame.display.DynamicLoreMetaCreatorRegistry
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RawTooltipIndex
import cc.mewcraft.wakame.display.RawTooltipKey
import cc.mewcraft.wakame.display.RendererConfig
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipKeyProvider
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.stream.Stream

// 文件说明:
// 这里是 CoreAttribute 的所有跟提示框渲染相关的代码

@ReloadDependency(
    runAfter = [RendererConfig::class]
)
@PostWorldDependency(
    runAfter = [RendererConfig::class]
)
internal object CoreAttributeBootstrap : Initializable, KoinComponent {
    private val dynamicLoreMetaCreatorRegistry by inject<DynamicLoreMetaCreatorRegistry>()

    override fun onPostWorld() {
        dynamicLoreMetaCreatorRegistry.register(CoreAttributeLoreMetaCreator())
    }
}

internal class CoreAttributeLoreMetaCreator : DynamicLoreMetaCreator {
    private val operationRawLines = DisplaySupport.RENDERER_GLOBAL_CONFIG_PROVIDER.entry<List<String>>(DisplaySupport.RENDERER_CONFIG_LAYOUT_NODE_NAME, "operation")
    private val elementRawLines = DisplaySupport.RENDERER_GLOBAL_CONFIG_PROVIDER.entry<List<String>>(DisplaySupport.RENDERER_CONFIG_LAYOUT_NODE_NAME, "element")

    override val namespace: String = Namespaces.ATTRIBUTE

    override fun test(rawLine: String): Boolean {
        return Key(rawLine).namespace() == namespace
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        val derivationRule = CoreAttributeLoreMeta.Derivation(operationIndex = operationRawLines, elementIndex = elementRawLines)
        return CoreAttributeLoreMeta(rawTooltipKey = Key(rawLine), rawTooltipIndex = rawTooltipIndex, defaultText = default, derivation = derivationRule)
    }
}

internal data class CoreAttributeLoreMeta(
    override val rawTooltipKey: RawTooltipKey,
    override val rawTooltipIndex: RawTooltipIndex,
    override val defaultText: List<Component>?,
    private val derivation: Derivation,
) : DynamicLoreMeta {
    /**
     * 根据以下衍生规则:
     * - attribute:{id}:{operation}           <-- 第一种
     * - attribute:{id}:{operation}:{element} <-- 第二种
     *
     * 为该属性生成所有的 [TooltipKey]s.
     */
    override fun generateTooltipKeys(): List<TooltipKey> {
        if (rawTooltipKey == AttributeRegistry.EMPTY_KEY) {
            return listOf(rawTooltipKey) // for `empty`, do not derive
        }

        val namespace = rawTooltipKey.namespace()
        val values = StringCombiner(rawTooltipKey.value(), ".") {
            addList(derivation.operationIndex)
            addList(derivation.elementIndex, AttributeRegistry.FACADES[rawTooltipKey].components.hasComponent<AttributeComponent.Element>())
        }.combine()

        return values.map { Key(namespace, it) }
    }

    override fun createDefault(): List<LoreLine>? {
        if (defaultText.isNullOrEmpty()) {
            return null
        }
        return generateTooltipKeys().map { key -> LoreLine.simple(key, defaultText) }
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

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("operationIndex", operationIndex),
                ExaminableProperty.of("elementIndex", elementIndex),
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
}

internal class CoreAttributeTooltipKeyProvider(
    private val config: RendererConfig,
) : TooltipKeyProvider<CoreAttribute> {
    override fun get(obj: CoreAttribute): TooltipKey? {
        // 属性的 tooltip key 目前有两种
        //   attribute:{facade_id}.{operation}
        //   attribute:{facade_id}.{operation}.{element}

        val rawTooltipKey = obj.key
        if (rawTooltipKey !in config.rawTooltipKeys) {
            return null
        }

        val operation = obj.operation
        val element = obj.element
        val tooltipKey = getOrPut(rawTooltipKey, operation, element) {
            Key(rawTooltipKey.namespace(), buildString {
                // append raw tooltip key
                append(rawTooltipKey.value())

                // append operation
                append("."); append(operation.key)

                // append element if it presents
                element?.let { append("."); append(it.uniqueId) }
            })
        }

        return tooltipKey
    }

    /**
     * This companion object serves as a singleton Map indexed by double or triple keys.
     */
    private companion object Index {
        /**
         * The tooltip keys in this map are double-indexed by `key` + `operation`.
         */
        val INDEX_FOR_COMBINATION_1: AttributeDoubleKeyTable<Key, Operation, TooltipKey> by ReloadableProperty { AttributeDoubleKeyTable() }

        /**
         * The tooltip keys in this map are triple-indexed by `key` + `operation` + `element`.
         */
        val INDEX_FOR_COMBINATION_2: AttributeTripleKeyTable<Key, Operation, Element, TooltipKey> by ReloadableProperty { AttributeTripleKeyTable() }

        /**
         * Caches a tooltip key from the given triple indexes.
         */
        fun put(rawTooltipKey: RawTooltipKey, operation: Operation, element: Element?, fullKey: TooltipKey) {
            if (element == null) {
                INDEX_FOR_COMBINATION_1
                    .getOrPut(rawTooltipKey) { Reference2ObjectOpenHashMap(4, 0.9f) } // 运算模式最多3个
                    .put(operation, fullKey)
            } else {
                INDEX_FOR_COMBINATION_2
                    .getOrPut(rawTooltipKey) { Reference2ObjectOpenHashMap(4, 0.9f) }
                    .getOrPut(operation) { Reference2ObjectOpenHashMap(8, 0.9f) } // 元素一般最多8个
                    .put(element, fullKey)
            }
        }

        /**
         * Gets a tooltip key from the given triple indexes.
         */
        fun get(rawTooltipKey: RawTooltipKey, operation: Operation, element: Element?): TooltipKey? {
            return if (element == null) {
                INDEX_FOR_COMBINATION_1[rawTooltipKey]?.get(operation)
            } else {
                INDEX_FOR_COMBINATION_2[rawTooltipKey]?.get(operation)?.get(element)
            }
        }

        /**
         * Returns the tooltip key for the given triple indexes ([rawTooltipKey], [operation],
         * [element]) if the tooltip key is cached and not `null`. Otherwise, calls
         * the [defaultValue] function, puts its result into the cache under the
         * given indexes and returns the call result.
         *
         * Note that the operation is not guaranteed to be atomic if the map is
         * being modified concurrently.
         */
        inline fun getOrPut(
            rawTooltipKey: RawTooltipKey,
            operation: Operation,
            element: Element?,
            defaultValue: () -> TooltipKey,
        ): TooltipKey {
            val value = get(rawTooltipKey, operation, element)
            return if (value == null) {
                val answer = defaultValue()
                put(rawTooltipKey, operation, element, answer)
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
        fun remove(
            rawTooltipKey: RawTooltipKey,
            operation: Operation,
            element: Element? = null,
        ): TooltipKey? {
            if (element == null) {
                val map1 = INDEX_FOR_COMBINATION_1[rawTooltipKey] ?: return null
                val value = map1.remove(operation)
                if (map1.isEmpty()) {
                    INDEX_FOR_COMBINATION_1.remove(rawTooltipKey)
                }
                return value
            } else {
                val map1 = INDEX_FOR_COMBINATION_2[rawTooltipKey] ?: return null
                val map2 = map1[operation] ?: return null
                val value = map2[element]
                if (map2.isEmpty()) {
                    map1.remove(operation)
                    if (map1.isEmpty()) {
                        INDEX_FOR_COMBINATION_2.remove(rawTooltipKey)
                    }
                }
                return value
            }
        }
    }
}

private typealias AttributeDoubleKeyTable<K1, K2, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, V>>
private typealias AttributeTripleKeyTable<K1, K2, K3, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, Reference2ObjectOpenHashMap<K3, V>>>

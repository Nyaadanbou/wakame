package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.composite.CompositeAttributeComponent
import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.registry.*
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

// TODO 移动到专门的渲染系统里

// 文件说明:
// 这里是 CoreAttribute 的所有跟提示框渲染相关的代码

@ReloadDependency(
    runAfter = [RendererBootstrap::class]
)
@PostWorldDependency(
    runAfter = [RendererBootstrap::class]
)
internal object AttributeCoreBootstrap : Initializable, KoinComponent {
    private val dynamicLoreMetaCreators by inject<DynamicLoreMetaCreators>()

    override fun onPostWorld() {
        for ((systemName, system) in ItemRenderers.entries()) {
            dynamicLoreMetaCreators.register(systemName, system.attributeCoreLoreMetaCreator)
        }
    }
}

internal class AttributeCoreLoreMetaCreator(
    config: ConfigProvider
) : DynamicLoreMetaCreator {
    private val operationRawLines = config.entry<List<String>>("operation")
    private val elementRawLines = config.entry<List<String>>("element")

    override val namespace: String = Namespaces.ATTRIBUTE

    override fun test(rawLine: String): Boolean {
        return Key.key(rawLine).namespace() == namespace
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        val derivationRule = AttributeCoreLoreMeta.Derivation(
            operationIndex = operationRawLines,
            elementIndex = elementRawLines
        )
        return AttributeCoreLoreMeta(
            rawTooltipKey = Key.key(rawLine),
            rawTooltipIndex = rawTooltipIndex,
            defaultText = default,
            derivation = derivationRule
        )
    }
}

internal data class AttributeCoreLoreMeta(
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
        val attributeId = rawTooltipKey.value()
        val values = StringCombiner(attributeId, ".") {
            addList(derivation.operationIndex)
            addList(derivation.elementIndex, AttributeRegistry.FACADES[attributeId].components.hasComponent<CompositeAttributeComponent.Element>())
        }.combine()

        return values.map { Key.key(namespace, it) }
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
            this.operationIndex.forEach { Operation.byKeyOrThrow(it) }
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

private typealias AttributeDoubleKeyTable<K1, K2, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, V>>
private typealias AttributeTripleKeyTable<K1, K2, K3, V> = Object2ObjectOpenHashMap<K1, Reference2ObjectOpenHashMap<K2, Reference2ObjectOpenHashMap<K3, V>>>

internal class AttributeCoreTooltipKeyProvider(
    private val config: RendererConfig,
) : TooltipKeyProvider<AttributeCore> {
    override fun get(obj: AttributeCore): TooltipKey? {
        // 属性的 tooltip key 目前有两种
        //   attribute:{facade_id}.{operation}
        //   attribute:{facade_id}.{operation}.{element}

        val rawTooltipKey = obj.id
        if (rawTooltipKey !in config.rawTooltipKeys) {
            return null
        }

        val operation = obj.attribute.operation
        val element = obj.attribute.element
        val tooltipKey = getOrPut(rawTooltipKey, operation, element) {
            Key.key(rawTooltipKey.namespace(), buildString {
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

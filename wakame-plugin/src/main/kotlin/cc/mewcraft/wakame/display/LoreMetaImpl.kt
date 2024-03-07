package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import net.kyori.adventure.text.Component

// TODO 统一 key 的生成规则

/**
 * 代表一个自定义固定内容的 [LoreMeta].
 */
internal data class CustomFixedLoreMeta(
    override val rawIndex: RawIndex,
    override val companionNamespace: String?,
    override val components: List<Component>,
) : FixedLoreMeta

/**
 * 代表一个“无字符”固定内容的 [LoreMeta].
 */
internal data class EmptyFixedLoreMeta(
    override val rawIndex: RawIndex,
    override val companionNamespace: String?,
) : FixedLoreMeta {
    override val components: List<Component> = listOf(Component.empty())
}

/**
 * 代表一个元数据的 [LoreMeta].
 */
internal data class MetaLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override val fullKeys: List<FullKey> =
        listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
}

/**
 * 代表一个技能的 [LoreMeta].
 */
internal data class AbilityLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override val fullKeys: List<FullKey> =
        listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
}

/**
 * 代表一个属性的 [LoreMeta].
 */
internal data class AttributeLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
    private val derivation: Derivation,
) : DynamicLoreMeta {
    data class Derivation(
        /**
         * 运算模式的顺序。
         */
        val operationIndex: List<String>,
        /**
         * 元素种类的顺序。
         */
        val elementIndex: List<String>,
    )

    /**
     * 根据以下衍生规则:
     * - attribute:id
     * - attribute:id:operation
     * - attribute:id:operation:element
     *
     * 为该属性生成所有的 full keys
     */
    override val fullKeys: List<FullKey>
        get() {
            if (rawKey == AttributeRegistry.EMPTY_KEY) {
                return listOf(rawKey) // for `empty`, do not derive
            }

            val ret = ArrayList<FullKey>()
            val meta = AttributeRegistry.getMeta(rawKey)
            val namespace = rawKey.namespace()
            val rawValue = StringBuilder(rawKey.value())
            for (operation in derivation.operationIndex) {
                requireNotNull(AttributeModifier.Operation.byKeyOrNull(operation)) { "Unknown attribute modifier operation: '$operation'" }
                if (!meta.element) {
                    ret += FullKey.key(namespace, "$rawValue.$operation")
                } else {
                    for (element in derivation.elementIndex) {
                        requireNotNull(ElementRegistry.get(element)) { "Unknown element: '$element'" }
                        ret += FullKey.key(namespace, "$rawValue.$operation.$element")
                    }
                }
            }
            return ret
        }
}

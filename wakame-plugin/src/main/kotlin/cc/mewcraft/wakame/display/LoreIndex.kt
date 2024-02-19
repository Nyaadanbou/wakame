package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry

// TODO 统一 key 的生成规则

/**
 * 代表 Item Lore 中的内容的**顺序**。
 *
 * @see LoreLine
 * @see LoreIndexLookup
 */
internal sealed interface LoreIndex {
    /**
     * 在配置文件内的原始 Key（即在配置文件内未经衍生处理的字符串值）。
     */
    val rawKey: RawKey

    /**
     * 在配置文件内的原始 Index（即在配置文件中的 List 的位置）。
     */
    val rawIndex: Int

    /**
     * 生成所有的“完整 Key”（注意区别于“原始 Key”）。完整 Key 将用于最终的顺序查询。
     *
     * [Full Key List][computeFullKeys] 是由单个 [Raw Key][rawKey]
     * 经过一系列规则衍生出来的一个或多个 Full Key。某些内容的 Raw Key 没有衍生规则，因此这些内容的 Raw Key 与
     * Full Key 完全一致。而有些内容的 Raw Key 有衍生规则，因此它们的 Raw Key 就与 Full Key 不一致。
     */
    fun computeFullKeys(): List<FullKey>
}

/**
 * 代表一个当源数据不存在时改用空行替代的顺序。
 *
 * @param loreIndex 原来的 loreIndex
 */
internal class FallbackLoreIndex(
    private val loreIndex: LoreIndex,
) : LoreIndex {
    override val rawKey: RawKey = loreIndex.rawKey
    override val rawIndex: Int = loreIndex.rawIndex
    override fun computeFullKeys(): List<FullKey> = loreIndex.computeFullKeys()

    fun fallback(): EmptyFixedLoreIndex {
        return EmptyFixedLoreIndex(rawIndex)
    }
}

/**
 * 代表一个固定内容的顺序。
 *
 * @see CustomFixedLoreIndex
 * @see EmptyFixedLoreIndex
 */
internal sealed interface FixedLoreIndex : LoreIndex {
    override val rawKey: RawKey
        // 实际上，固定内容的 raw key 就是其在配置文件中原始顺序的字符串形式
        // 例如，这行固定内容配置文件列表中的第3个，那么它的 key 就是 "fixed:3"
        get() = RawKey.key("fixed", rawIndex.toString())

    override val rawIndex: Int

    override fun computeFullKeys(): List<FullKey> {
        // 固定内容的 full key 与其 raw key 的逻辑是一样的
        return listOf(rawKey)
    }
}

/**
 * 代表一个自定义的固定内容的顺序。
 */
internal data class CustomFixedLoreIndex(
    override val rawIndex: Int,
) : FixedLoreIndex

/**
 * 代表一个空的固定内容的顺序。
 */
internal data class EmptyFixedLoreIndex(
    override val rawIndex: Int,
) : FixedLoreIndex

/**
 * 代表一个元数据的顺序。
 */
internal data class MetaLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int,
) : LoreIndex {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}

/**
 * 代表一个技能的顺序。
 */
internal data class AbilityLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int,
) : LoreIndex {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}

/**
 * 代表一个属性的顺序。
 */
internal data class AttributeLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int,
    /**
     * 表示配置文件内 operation 与 element 顺序的规则
     */
    val rule: Rule,
) : LoreIndex {

    data class Rule(
        val operationIndex: List<String>,
        val elementIndex: List<String>
    )

    /**
     * 根据以下衍生规则:
     *   - attribute:id
     *   - attribute:id:operation
     *   - attribute:id:operation:element
     *
     * 为该属性生成所有的 full keys
     */
    override fun computeFullKeys(): List<FullKey> {
        val ret = mutableListOf<FullKey>()
        val meta = AttributeRegistry.getMeta(rawKey)

        for (operation in rule.operationIndex) {
            if (AttributeModifier.Operation.byKeyOrNull(operation) == null) continue

            ret.add(FullKey.key(rawKey.namespace(), "${rawKey.value()}.$operation"))

            if (meta.element) {
                for (element in rule.elementIndex) {
                    if (ElementRegistry.get(element) == null) continue

                    ret.add(FullKey.key(rawKey.namespace(), "${rawKey.value()}.$operation.$element"))
                }
            }
        }

        return ret
    }
}

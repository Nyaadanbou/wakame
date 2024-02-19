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
 * 代表当源数据不存在时，改用空行替代的一行。
 */
// TODO 理论上除了固定内容(FixedLoreLine)，都需要继承这个接口
internal sealed interface FallbackLoreIndex : LoreIndex {
    val sourceFullKey: FullKey
}

/**
 * 代表其内容始终固定不变的一行的索引。
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

internal data class AttributeLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int,
) : LoreIndex {

    override fun computeFullKeys(): List<FullKey> {
        /*
         * 根据以下衍生规则：
         *    - attribute:id
         *    - attribute:id:operation
         *    - attribute:id:operation:element
         * 为该属性生成所有的 full keys
         */

        val ret = mutableListOf<FullKey>()
        val meta = AttributeRegistry.getMeta(rawKey)

        for (operation in AttributeModifier.Operation.entries.map { it.key }) {
            // FIXME 根据 renderer_order.operation 定义的顺序添加 运算模式
            ret.add(FullKey.key(rawKey.namespace(), "${rawKey.value()}.$operation"))

            if (meta.element) {
                // FIXME 根据 renderer_order.element 定义的顺序添加 元素
                for (element in ElementRegistry.values) {
                    ret.add(FullKey.key(rawKey.namespace(), "${rawKey.value()}.$operation.${element.key}"))
                }
            }
        }

        return ret
    }
}

internal data class CustomFixedLoreIndex(
    override val rawIndex: Int,
) : FixedLoreIndex

internal data class EmptyFixedLoreIndex(
    override val rawIndex: Int,
) : FixedLoreIndex

internal data class MetaLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int,
) : LoreIndex {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}

internal data class AbilityLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int,
) : LoreIndex {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}
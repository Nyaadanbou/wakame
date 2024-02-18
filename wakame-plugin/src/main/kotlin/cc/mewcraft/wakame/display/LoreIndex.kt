package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry

sealed interface LoreIndex {
    /**
     * 在配置文件内的原始 Key.
     */
    val rawKey: RawKey

    /**
     * 在配置文件内的原始索引（即在配置文件中的 List 的位置）
     */
    val rawIndex: Int

    /**
     * 生成完整 Key（注意区别于“原始 Key”）。完整 Key 将用于最终的顺序查询。
     *
     * [Full Key List][computeFullKeys] 是由单个 [Raw Key][rawKey] 经过一系列规则衍生出来的一个或多个 Key。
     * 某些内容的 Raw Key 没有衍生规则，因此这些内容的 Raw Key 与 Full Key 是完全一致的。
     * 而有些内容的 Raw Key 有衍生规则，因此它们的 Raw Key 就与 Full Key 不一致。
     */
    fun computeFullKeys(): List<FullKey>
}

internal data class AttributeLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int,
) : LoreIndex {

    override fun computeFullKeys(): List<FullKey> {
        /*
        创建属于这个属性的下面三种所有参数：
            - attribute:属性
            - attribute:属性:operation
            - attribute:属性:operation:element
         */
        val attributeMeta = AttributeRegistry.getMeta(rawKey)

        val fullKeys = mutableListOf<FullKey>()
        for (operation in AttributeModifier.Operation.entries.map { it.key }) {
            fullKeys.add(FullKey.key(rawKey.namespace(), "${rawKey.value()}.$operation"))

            if (!attributeMeta.element) {
                continue
            }

            for (element in ElementRegistry.values) {
                fullKeys.add(FullKey.key(rawKey.namespace(), "${rawKey.value()}.$operation.${element.key}"))
            }
        }

        return fullKeys
    }
}

internal data class FixedLoreIndex(
    override val rawIndex: Int,
    val isEmptyLine: Boolean
) : LoreIndex {
    override val rawKey: RawKey = fullKey

    override fun computeFullKeys(): List<FullKey> {
        return listOf(fullKey)
    }
}

internal val FixedLoreIndex.fullKey: FullKey
    get() = FullKey.key(rawIndex.toString())

internal data class MetaLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int
) : LoreIndex {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}

internal data class AbilityLoreIndex(
    override val rawKey: RawKey,
    override val rawIndex: Int
) : LoreIndex {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}
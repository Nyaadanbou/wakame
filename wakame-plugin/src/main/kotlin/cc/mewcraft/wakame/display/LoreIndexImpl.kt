package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry

// TODO 统一 key 的生成规则

/**
 * 代表一个当源数据不存在时改用空行替代的顺序。
 *
 * @param sourceLoreIndex 原来的 loreIndex
 */
internal data class FallbackLoreIndex( // TODO unfinished
    private val sourceLoreIndex: LoreIndex,
) : LoreIndex {
    override val rawKey: RawKey = sourceLoreIndex.rawKey
    override val rawIndex: Int = sourceLoreIndex.rawIndex
    override fun computeFullKeys(): List<FullKey> = sourceLoreIndex.computeFullKeys()

    fun fallback(): EmptyFixedLoreIndex {
        return EmptyFixedLoreIndex(rawIndex)
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

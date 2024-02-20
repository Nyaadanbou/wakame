package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry

// TODO 统一 key 的生成规则

/**
 * 代表一个自定义的固定内容的顺序。
 */
internal data class CustomFixedLoreMeta(
    override val rawIndex: RawIndex,
    override val requiredNamespace: String?,
    override val canBeEmptyLine: Boolean
) : FixedLoreMeta

/**
 * 代表一个空的固定内容的顺序。
 */
internal data class EmptyFixedLoreMeta(
    override val rawIndex: RawIndex,
    override val requiredNamespace: String?,
    override val canBeEmptyLine: Boolean
) : FixedLoreMeta

/**
 * 代表一个元数据的顺序。
 */
internal data class MetaLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val canBeEmptyLine: Boolean = true,
) : LoreMeta {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}

/**
 * 代表一个技能的顺序。
 */
internal data class AbilityLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val canBeEmptyLine: Boolean = true,
) : LoreMeta {
    override fun computeFullKeys(): List<FullKey> {
        return listOf(FullKey.key(rawKey.namespace(), rawKey.value()))
    }
}

/**
 * 代表一个属性的顺序。
 */
internal data class AttributeLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    /**
     * 表示配置文件内 operation 与 element 顺序的规则
     */
    val rule: Rule,
    override val canBeEmptyLine: Boolean = true,
) : LoreMeta {

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

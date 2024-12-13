package cc.mewcraft.wakame.display2

import net.kyori.adventure.text.Component

/**
 * 用于创建 [TextMetaFactory] 实例的工厂函数.
 */
fun interface TextMetaFactory {

    companion object {
        /**
         * 创建一个 [TextMetaFactory] 实例, 用于创建 [FixedSimpleTextMeta] 实例.
         */
        fun fixed(): TextMetaFactory = TextMetaFactory(::FixedSimpleTextMeta)
    }

    /**
     * 创建一个新的 [SimpleTextMeta], 其命名空间应该与 *预设的* 一致.
     *
     * @param sourceIndex the line identity (without any derivation)
     * @param sourceOrdinal the source ordinal
     * @param defaultText the default text (can be `null`)
     * @return a new instance of [SimpleTextMeta]
     *
     * @throws IllegalArgumentException if the [sourceIndex] is unrecognized
     */
    fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta
}

/**
 * 如果要创建的 [TextMeta] 符合 [FixedSimpleTextMeta] 的描述, 可以使用这个构造函数.
 */
fun TextMetaFactory(): TextMetaFactory {
    return TextMetaFactory { sourceIndex, sourceOrdinal, defaultText ->
        FixedSimpleTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }
}

/**
 * 用来描述不会衍生并且只有一个 [SourceIndex] 的 [IndexedText].
 *
 * 例如: 标准渲染器中的 `lore`, `level`, `enchantment` 等.
 * 与之相反的是那些会衍生的 [IndexedText], 例如 `attribute`.
 */
private data class FixedSimpleTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
) : SimpleTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()

    override fun deriveIndexes(): List<DerivedIndex> {
        return listOf(sourceIndex)
    }
}
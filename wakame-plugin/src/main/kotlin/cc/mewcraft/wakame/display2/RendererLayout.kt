package cc.mewcraft.wakame.display2

import net.kyori.examination.Examinable

/**
 * 封装了渲染布局.
 */
interface RendererLayout : Examinable {
    /**
     * 静态的渲染内容. 这些内容完全由配置决定, 而非物品本身.
     */
    val staticIndexedTextList: List<IndexedText>

    /**
     * 默认的渲染内容. 当源数据不存在时将采用这里的默认值.
     */
    val defaultIndexedTextList: List<IndexedText>

    /**
     * 获取指定的 [index] 对应的*位置顺序*.
     * 位置顺序由 [DerivedTooltipOrdinal] 表示, 数值越小, 越靠前面.
     *
     * 如果 [index] 没有对应的位置顺序, 返回 `null`.
     */
    fun getOrdinal(index: DerivedTooltipIndex): DerivedTooltipOrdinal?

    /**
     * 获取指定的 [index] 对应的*元数据*.
     *
     * 如果 [index] 没有对应的元数据, 返回 `null`.
     */
    fun <T : TextMeta> getMetadata(index: DerivedTooltipIndex): T?
}

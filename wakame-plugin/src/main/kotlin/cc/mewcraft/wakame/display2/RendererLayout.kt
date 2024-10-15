package cc.mewcraft.wakame.display2

import net.kyori.examination.Examinable

/**
 * 封装了渲染布局.
 */
interface RendererLayout : Examinable {
    /**
     * 静态的渲染内容.
     *
     * 这些内容 *本身* 完全由配置决定, 而非物品堆叠.
     * 但这些内容可以根据物品的具体数据而选择不显示.
     */
    val staticIndexedTextList: List<IndexedText>

    /**
     * 默认的渲染内容. 规则如下:
     *
     * - 当物品上的特定数据不存在时, 将采用这里的默认值.
     * - 如果物品上的特定数据存在时, 默认值将不会被使用 (显然).
     * - 如果物品上的特定数据不存在, 同时默认值也不存在, 将不会显示任何内容.
     */
    val defaultIndexedTextList: List<IndexedText>

    /**
     * 获取指定的 [index] 对应的*位置顺序*.
     * 位置顺序由 [DerivedOrdinal] 表示, 数值越小, 越靠前面.
     *
     * 如果 [index] 没有对应的位置顺序, 返回 `null`.
     */
    fun getOrdinal(index: DerivedIndex): DerivedOrdinal?

    /**
     * 获取指定的 [index] 对应的*元数据*.
     *
     * 如果 [index] 没有对应的元数据, 返回 `null`.
     */
    fun <T : TextMeta> getMetadata(index: DerivedIndex): T?
}

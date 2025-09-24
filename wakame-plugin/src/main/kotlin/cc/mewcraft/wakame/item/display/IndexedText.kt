package cc.mewcraft.wakame.item.display

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * 代表一个带有索引的文本内容, 用来封装物品提示框中的特定文本内容.
 */
sealed interface IndexedText {
    val idx: Key
    val text: List<Component>

    companion object {
        @JvmField
        val NOP: IndexedText = _root_ide_package_.cc.mewcraft.wakame.item.display.NoOpIndexedText
    }
}

/**
 * 一个空的 [cc.mewcraft.wakame.item.display.IndexedText] 实例, 用于表示该内容不应该被渲染.
 *
 * 推荐用法: 使用 [IndexedText.NOP] 来获取该实例.
 */
data object NoOpIndexedText : IndexedText {
    override val idx: Key = Key.key("internal", "noop")
    override val text: List<Component> = emptyList()
}

/**
 * [IndexedText] 的标准实现, 用于封装大部分需要被渲染的物品数据.
 */
data class SimpleIndexedText(
    override val idx: Key,
    override val text: List<Component>,
) : IndexedText

/**
 * 文本内容不由世界的状态决定, 而是由插件配置决定的 [IndexedText].
 */
data class StaticIndexedText(
    override val idx: Key,
    override val text: List<Component>,
) : IndexedText

/**
 * 文本内容由传入的 [supplier] 动态提供, 每次调用 [text] 时都会重新生成.
 */
data class DynamicIndexedText(
    override val idx: Key,
    private val supplier: Supplier<List<Component>>,
) : IndexedText, Examinable {
    override val text: List<Component>
        get() = supplier.get()

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("idx", idx),
        ExaminableProperty.of("text", text)
    )

    override fun toString(): String = StringExaminer.simpleEscaping().examine(this)
}
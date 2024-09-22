package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * 代表一个带有索引的文本内容.
 */
sealed interface IndexedText {
    val idx: Key
    val text: List<Component>
}

// /**
//  * 包含跟 [IndexedText] 相关的全局变量和函数.
//  */
// object IndexedTexts


/**
 * [IndexedText] 的标准实现, 用于大部分物品组件.
 */
@ConsistentCopyVisibility
data class SimpleIndexedText
internal constructor(
    override val idx: Key,
    override val text: List<Component>,
) : IndexedText

/**
 * 文本内容不由世界状态决定, 而是由配置文件决定的 [IndexedText].
 */
@ConsistentCopyVisibility
data class StaticIndexedText
internal constructor(
    override val idx: Key,
    override val text: List<Component>,
) : IndexedText

/**
 * 文本内容由传入的 [supplier] 动态提供, 每次调用 [text] 时都会重新生成.
 */
class DynamicIndexedText
internal constructor(
    override val idx: Key,
    private val supplier: Supplier<List<Component>>,
) : IndexedText, Examinable {
    override val text: List<Component>
        get() = supplier.get()

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("idx", idx),
        ExaminableProperty.of("text", text)
    )

    override fun toString(): String = toSimpleString()
}
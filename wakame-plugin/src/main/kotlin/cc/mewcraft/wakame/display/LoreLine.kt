package cc.mewcraft.wakame.display

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * 代表 [Item Lore](https://minecraft.wiki/w/Data_component_format#lore) 中的内容.
 *
 * 注意与 [LoreMeta] 区别 - [LoreMeta] 描述的是顺序及其他信息, 而 [LoreLine] 仅代表内容.
 *
 * @see LoreMeta
 * @see LoreMetaLookup
 */
interface LoreLine {
    /**
     * 能够唯一识别这一内容的标识.
     */
    val key: TooltipKey

    /**
     * 最终生成的文本内容. 如果有多行则该列表就有多个元素.
     */
    val content: List<Component>

    /**
     * 该内容是否应该显示.
     */
    val shouldShow: Boolean
        get() = this !is NoopLoreLine

    /**
     * 该内容是否是始终不变的.
     */
    val isConstant: Boolean
        get() = this is ConstantLoreLine

    /**
     * This companion object provides constructor functions of [LoreLine].
     */
    companion object {
        fun noop(): LoreLine {
            return NoopLoreLine
        }

        fun constant(key: TooltipKey, lines: List<Component>): LoreLine {
            return ConstantLoreLine(key, lines)
        }

        fun simple(key: TooltipKey, lines: List<Component>): LoreLine {
            return SimpleLoreLine(key, lines)
        }

        fun supply(key: TooltipKey, provider: Supplier<List<Component>>): LoreLine {
            return SupplierLoreLine(key, provider)
        }
    }
}

/**
 * 代表一个不会显示的内容.
 */
private data object NoopLoreLine : LoreLine {
    override val key: TooltipKey = GenericKeys.NOOP
    override val content: List<Component> = emptyList()
}

/**
 * 代表一个始终不变的内容(位置也不变).
 */
private data class ConstantLoreLine(
    override val key: TooltipKey,
    override val content: List<Component>,
) : LoreLine

/**
 * 代表一个一般的内容, 例如属性, 技能等.
 */
private data class SimpleLoreLine(
    override val key: TooltipKey,
    override val content: List<Component>,
) : LoreLine

/**
 * 跟 [SimpleLoreLine] 一样, 只不过 [provider] 由 [Provider] 提供.
 */
private class SupplierLoreLine(
    override val key: TooltipKey,
    val provider: Supplier<List<Component>>,
) : LoreLine, Examinable {
    override val content: List<Component>
        get() = provider.get()

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("content", provider)
    )

    override fun toString(): String = toSimpleString()
}
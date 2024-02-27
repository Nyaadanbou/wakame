package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import net.kyori.adventure.text.Component
import kotlin.reflect.KClass

/**
 * 为给定 [NekoItemStack] 的 Name 和 Lore 生成格式化后的内容。
 */
internal interface TextStylizer {

    /**
     * 为给定的 [item] 生成名字。
     *
     * 不像 [stylizeLore]，该函数生成的内容可以直接用在物品上。
     *
     * 该函数不会修改给定的 [item].
     *
     * @param item 要生成名字的物品
     * @return 生成的物品名字
     */
    fun stylizeName(item: NekoItemStack): Component

    /**
     * Generates [lore lines][LoreLine] from the [item]. The returned
     * [lore lines][LoreLine] need to be **finalized** before they are used
     * on [item]. Also, the returned collection should not contain any
     * [fixed lore lines][FixedLoreLine].
     *
     * This function won't modify the given [item].
     *
     * @param item the item to generate lore for
     * @return the generated lore lines
     */
    fun stylizeLore(item: NekoItemStack): Collection<LoreLine>

}

/**
 * To be used by [TextStylizer].
 */
internal interface AbilityStylizer {
    fun stylize(core: BinaryAbilityCore): List<Component>
}

/**
 * To be used by [TextStylizer].
 */
internal interface AttributeStylizer {
    fun stylize(core: BinaryAttributeCore): List<Component>

    interface AttackSpeedFormat {
        /**
         * 攻速的格式。
         */
        val merged: String

        /**
         * 必须包含9个元素，每个对应一个攻速等级。
         */
        val levels: Map<Int, String>
    }
}

/**
 * To be used by [TextStylizer].
 */
internal interface OperationStylizer {
    fun stylize(value: String, operation: AttributeModifier.Operation): String
}

/**
 * To be used by [TextStylizer].
 */
internal interface ItemMetaStylizer {

    /* First is the `name` stylizer. */

    /**
     * Stylizes the given [name].
     *
     * This function is intentionally separated from the lore stylization.
     */
    fun stylizeName(name: String): Component

    /* Following are `lore` stylizers. */

    fun interface ChildStylizer<I : BinaryItemMeta<*>> {
        fun stylize(input: I): List<Component>
    }

    /**
     * To implementer: Every [BinaryItemMeta] to be rendered should have a
     * corresponding [ChildStylizer] in this map.
     *
     * Map specifications: `map key` is class of [BinaryItemMeta] and `map value` is
     * corresponding [stylizer][ChildStylizer].
     */
    val childStylizerMap: Map<KClass<out BinaryItemMeta<*>>, ChildStylizer<*>>

    /**
     * Gets child stylizer by class.
     */
    fun <I : BinaryItemMeta<*>> getChildStylizerBy(clazz: KClass<out I>): ChildStylizer<I>

    interface LoreFormat {
        /**
         * The format of a single line.
         */
        val line: String

        /**
         * **Never be an empty list.** Use `null` to indicate "don't add header".
         */
        val header: List<String>?

        /**
         * **Never be an empty list.** Use `null` to indicate "don't add bottom".
         */
        val bottom: List<String>?
    }

    interface ListFormat {
        /**
         * The format of all elements joined together.
         */
        val merged: String

        /**
         * The format of a single element.
         */
        val single: String

        /**
         * The format of the separator.
         */
        val separator: String
    }
}

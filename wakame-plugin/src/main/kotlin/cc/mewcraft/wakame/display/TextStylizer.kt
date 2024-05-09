package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.item.binary.meta.BDisplayNameMeta
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import kotlin.reflect.KClass

/**
 * Generates stylized name and lore for a [NekoStack].
 */
internal interface TextStylizer {

    /**
     * Generates a custom name for the [item]。
     *
     * Unlike [stylizeLore], the returned component is ready to be used
     * on the item.
     *
     * This function won't modify the given [item].
     *
     * @param item the item to generate name for
     * @return the generated name
     */
    fun stylizeName(item: NekoStack): Component

    /**
     * Generates [lore lines][LoreLine] for the [item]. The returned
     * [lore lines][LoreLine] need to be **finalized** before they are used
     * on [item]. Also, the returned collection should not contain any
     * [constant lore lines][ConstantLoreLine].
     *
     * This function won't modify the given [item].
     *
     * @param item the item to generate lore for
     * @return the generated lore lines
     */
    fun stylizeLore(item: NekoStack): Collection<LoreLine>

}

/**
 * To be used by [TextStylizer].
 */
internal interface SkillStylizer {
    fun stylize(core: BinarySkillCore): List<Component>
}

/**
 * To be used by [TextStylizer].
 */
internal interface AttributeStylizer {
    fun stylize(core: BinaryAttributeCore): List<Component>

    /**
     * 所有属性的渲染格式。
     *
     * ## 映射说明
     * - `map key` 跟 [AttributeRegistry] 里的一致，不是 [FullKey]
     * - `map value` 就是配置文件里对应的字符串值，无需做任何处理
     *
     * **注意该映射不包含 [Attributes.ATTACK_SPEED_LEVEL]**
     */
    interface AttributeFormat : Examinable {
        val values: Map<Key, String>
    }

    interface AttackSpeedFormat : Examinable {
        /**
         * 攻速的格式。
         */
        val merged: String

        /**
         * 必须包含9个元素，每个对应一个攻速等级。
         */
        val levels: Map<Int, String>
    }

    interface OperationFormat : Examinable {
        val values: Map<AttributeModifier.Operation, String>
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
     * Stylizes the name of given [item] and returns the component.
     */
    fun stylizeName(item: NekoStack): Component

    /* Following are `lore` stylizers. */

    /**
     * A child stylizer is simply a stylizer abstraction of a certain
     * type of content in the lore. See the implementations for details.
     *
     * @param M the input binary item meta
     */
    fun interface ChildStylizer<M : BinaryItemMeta<*>> {
        /**
         * Stylizes the item meta and returns a list of components.
         *
         * @param input the input
         * @return stylized content
         */
        fun stylize(input: M): List<Component>
    }

    /**
     * Gets child stylizer by the class of [BinaryItemMeta].
     *
     * To implementer: Every [BinaryItemMeta] (except [BDisplayNameMeta]) to be rendered
     * should have a corresponding [ChildStylizer] in this map. In the case where no
     * implementation is found for the [clazz], a default [ChildStylizer] indicating
     * a missing implementation should be returned.
     *
     * This function never throws.
     */
    fun <M : BinaryItemMeta<*>> getChildStylizerBy(clazz: KClass<out M>): ChildStylizer<M>

    /**
     * The same as [getChildStylizerBy] but accepts an instance of [BinaryItemMeta].
     */
    fun <M : BinaryItemMeta<*>> getChildStylizerBy(itemMeta: M): ChildStylizer<M> {
        return getChildStylizerBy(itemMeta::class)
    }

    interface LoreFormat : Examinable {
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

    interface ListFormat : Examinable {
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

package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.text.Component
import java.util.UUID

/**
 * 为给定的 [NekoItemStack] 生成 [lore lines][LoreLine].
 *
 * 注意，该接口所所生成的 [lore lines][LoreLine]
 * 不能直接用在物品上，需要做进一步处理才可以用在物品上。例如排序，拆包，加上固定内容。
 */
internal interface LoreStylizer {

    /**
     * Generates [lore lines][LoreLine] from the [item]. The returned
     * collection should not contain any [fixed lore lines][FixedLoreLine].
     *
     * This function won't modify the given [item].
     *
     * @param item the item to generate lore for
     * @return the generated lore lines
     */
    fun stylize(item: NekoItemStack): Collection<LoreLine>

}

/**
 * To be used by [LoreStylizer].
 */
internal interface AbilityStylizer {
    fun stylizeAbility(core: BinaryAbilityCore): List<Component>
}

/**
 * To be used by [LoreStylizer].
 */
internal interface AttributeStylizer {
    fun stylizeAttribute(core: BinaryAttributeCore): List<Component>

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
 * To be used by [LoreStylizer].
 */
internal interface OperationStylizer {
    fun stylizeValue(value: String, operation: AttributeModifier.Operation): String
}

/**
 * To be used by [LoreStylizer].
 */
internal interface MetaStylizer {
    val nameFormat: String
    val loreFormat: LoreFormat
    val levelFormat: String
    val rarityFormat: String
    val elementFormat: ListFormat
    val kizamiFormat: ListFormat
    val skinFormat: String
    val skinOwnerFormat: String

    fun stylizeName(name: String): Component
    fun stylizeLore(lore: List<String>): List<Component>
    fun stylizeLevel(level: Int): List<Component>
    fun stylizeRarity(rarity: Rarity): List<Component>
    fun stylizeElement(elements: Set<Element>): List<Component>
    fun stylizeKizami(kizami: Set<Kizami>): List<Component>
    fun stylizeSkin(skin: ItemSkin): List<Component>
    fun stylizeSkinOwner(skinOwner: UUID): List<Component>

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
         * The format of the separator which separates the elements in the list.
         */
        val separator: String
    }
}

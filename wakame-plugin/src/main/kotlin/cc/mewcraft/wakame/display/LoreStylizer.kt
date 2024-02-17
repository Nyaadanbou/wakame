package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.text.Component
import java.util.UUID

/* 包含配置文件的抽象 */

/**
 * 为给定的 [NekoItemStack] 生成 [lore lines][LoreLine].
 *
 * 注意，该接口所所生成的 [lore lines][LoreLine]
 * 不能直接用在物品上，需要做进一步处理才可以用在物品上。例如排序，拆包，加上固定内容。
 */
internal interface LoreStylizer {

    /**
     * Generates [lore lines][LoreLine] for the given [nekoItemStack].
     *
     * This function won't modify the given [nekoItemStack].
     *
     * @param nekoItemStack the item to generate lore for
     * @return the generated lore lines
     */
    fun stylize(nekoItemStack: NekoItemStack): Collection<LoreLine>

}

// To g22: 下面都是 LoreStylizer 需要用到的实现

internal interface AbilityStylizer {
    fun stylizeAbility(ability: BinaryAbilityCore): List<Component>
}

internal interface AttributeStylizer {
    fun stylizeAttribute(attribute: BinaryAttributeCore): List<Component>
}

internal interface OperationStylizer {
    fun stylizeAddition(value: Double): String
    fun stylizeMultiplyBase(value: Double): String
    fun stylizeMultiplyTotal(value: Double): String
}

internal interface MetaStylizer {
    val nameStyle: String
    val loreStyle: LoreStyle
    val levelStyle: String
    val rarityStyle: String
    val elementStyle: ListStyle
    val kizamiStyle: ListStyle
    val skinStyle: String
    val skinOwnerStyle: String

    fun stylizeName(name: String): Component
    fun stylizeLore(lore: List<String>): List<Component>
    fun stylizeLevel(level: Int): List<Component>
    fun stylizeRarity(rarity: Rarity): List<Component>
    fun stylizeElement(elements: Set<Element>): List<Component>
    fun stylizeKizami(kizami: Set<Kizami>): List<Component>
    fun stylizeSkin(skin: ItemSkin): List<Component>
    fun stylizeSkinOwner(skinOwner: UUID): List<Component>

    class LoreStyle(
        val line: String,
        val header: List<String>?,
        val bottom: List<String>?,
    )

    class ListStyle(
        val merged: String,
        val single: String,
        val separator: String,
    )
}

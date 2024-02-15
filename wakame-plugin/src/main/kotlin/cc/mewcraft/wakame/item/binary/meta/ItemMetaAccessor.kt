package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.text.Component
import java.util.UUID

interface ItemMetaAccessor : ItemMetaSetter {
    /**
     * 物品的名字。
     */
    val name: Component?
    val nameOrEmpty: Component

    /**
     * 物品的额外描述。如果没有描述则返回空列表。
     */
    val lore: List<Component>?
    val loreOrEmpty: List<Component>

    /**
     * 物品的等级。不是所有物品都有等级，因此可能为空。
     */
    val level: Int?
    val levelOrThrow: Int

    /**
     * 物品的稀有度。不是所有物品都有稀有度，因此可能为空。
     */
    val rarity: Rarity?
    val rarityOrThrow: Rarity

    /**
     * 物品的元素。
     *
     * 如果该物品上有X元素的属性或技能，那么该集合一定会包含X元素。
     */
    val element: Set<Element>?
    val elementOrEmpty: Set<Element>

    /**
     * 物品的铭刻。
     */
    val kizami: Set<Kizami>?
    val kizamiOrEmpty: Set<Kizami>

    /**
     * 物品的皮肤。
     */
    val skin: ItemSkin?
    val skinOrThrow: ItemSkin

    /**
     * 物品的皮肤的所有者。
     */
    val skinOwner: UUID?
    val skinOwnerOrThrow: UUID
}
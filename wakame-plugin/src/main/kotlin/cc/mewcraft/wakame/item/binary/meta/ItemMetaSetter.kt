package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import java.util.UUID

/**
 * 提供若干函数来修改一个 [ItemMetaAccessor] 的状态。
 */
interface ItemMetaSetter {
    /**
     * Sets tags, overwriting any that are in `this`.
     */
    fun putRoot(compoundTag: CompoundShadowTag)

    /**
     * Sets name.
     */
    fun putName(name: String)

    /**
     * Sets extra lore, overwriting any that are in `this`.
     */
    fun putLore(lore: List<String>)

    /**
     * Sets level or clears it.
     */
    fun putLevel(level: Int?)

    /**
     * Sets rarity or clears it.
     */
    fun putRarity(rarity: Rarity?)

    /**
     * Sets elements, overwriting any that are in `this`.
     */
    fun putElements(elements: Collection<Element>)

    /**
     * Sets kizami, overwriting any that are in `this`.
     */
    fun putKizami(kizami: Collection<Kizami>)

    /**
     * Sets skin or clears it.
     *
     * @param skin an item skin, or `null` to clear it
     */
    fun putSkin(skin: ItemSkin?)

    /**
     * Sets skin owner or clears it.
     *
     * @param skinOwner a skin owner, or `null` to clear it
     */
    fun putSkinOwner(skinOwner: UUID?)
}
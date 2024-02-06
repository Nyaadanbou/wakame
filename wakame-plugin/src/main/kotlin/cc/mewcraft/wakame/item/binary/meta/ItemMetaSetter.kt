package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.text.Component
import java.util.*

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
     *
     * @param name the name
     */
    fun putName(name: Component)

    /**
     * Sets extra lore, overwriting any that are in `this`.
     *
     * @param lore the lore
     */
    fun putLore(lore: List<Component>)

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
    fun putElements(elements: Iterable<Element>)

    /**
     * Sets kizami, overwriting any that are in `this`.
     */
    fun putKizami(kizami: Iterable<Kizami>)

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
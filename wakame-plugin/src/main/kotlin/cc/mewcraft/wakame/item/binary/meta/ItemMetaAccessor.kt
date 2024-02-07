package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.text.Component
import java.util.UUID

interface ItemMetaAccessor : ItemMetaSetter {
    /**
     * Encompassing all tags of this [ItemMetaAccessor].
     */
    @InternalApi
    val tags: CompoundShadowTag // 外部不应该读取该变量

    /**
     * 物品的名字。
     */
    val name: Component

    /**
     * 物品的额外描述。如果没有描述则返回空列表。
     */
    val lore: List<Component>

    /**
     * 物品的等级。
     */
    val level: Int?
    val levelOrThrow: Int

    /**
     * 物品的稀有度。
     */
    val rarity: Rarity?
    val rarityOrThrow: Rarity

    /**
     * 物品的元素。如果没有元素则返回空集。
     *
     * 可以用来快速判断物品能打出什么元素效果。
     */
    val elements: Set<Element>

    /**
     * 物品的铭刻。如果没有铭刻则返回空集。
     */
    val kizami: Set<Kizami>

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
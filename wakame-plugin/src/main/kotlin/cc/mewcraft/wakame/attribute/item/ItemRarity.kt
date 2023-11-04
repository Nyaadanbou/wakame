package cc.mewcraft.wakame.attribute.item

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.ByteBinaryTag

/**
 * 物品稀有度
 */
class ItemRarity(
    /**
     * Identifier of this item rarity.
     */
    var id: Byte,
) : ItemAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "item_rarity")
    }

    override fun asAttributeModifier(): AttributeModifier {
        throw UnsupportedOperationException()
    }

    // Byte

    override fun save(): BinaryTag {
        return ByteBinaryTag.byteBinaryTag(id)
    }

    override fun load(tag: BinaryTag) {
        if (tag is ByteBinaryTag)
            id = tag.value()
    }
}
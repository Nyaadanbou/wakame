package cc.mewcraft.wakame.attribute.item

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.StringBinaryTag

/**
 * 物品铭刻
 */
class ItemSet(
    /**
     * Identifier of this item set.
     */
    var id: String,
) : ItemAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "item_set")
    }

    override fun asAttributeModifier(): AttributeModifier {
        throw UnsupportedOperationException()
    }

    // String

    override fun save(): BinaryTag {
        return StringBinaryTag.stringBinaryTag(id)
    }

    override fun load(tag: BinaryTag) {
        if (tag is StringBinaryTag)
            id = tag.value()
    }
}

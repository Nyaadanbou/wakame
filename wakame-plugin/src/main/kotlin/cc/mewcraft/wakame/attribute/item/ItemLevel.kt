package cc.mewcraft.wakame.attribute.item

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.ByteBinaryTag

/**
 * 物品等级
 */
class ItemLevel(
    var level: Int,
) : ItemAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "item_level")
    }

    override fun asAttributeModifier(): AttributeModifier {
        throw UnsupportedOperationException()
    }

    // Byte

    override fun save(): BinaryTag {
        return ByteBinaryTag.byteBinaryTag(level.toByte())
    }

    override fun load(tag: BinaryTag) {
        if (tag is ByteBinaryTag)
            level = tag.intValue()
    }
}
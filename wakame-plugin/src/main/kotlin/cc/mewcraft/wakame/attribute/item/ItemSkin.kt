package cc.mewcraft.wakame.attribute.item

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import cc.mewcraft.wakame.util.compoundBinaryTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.util.*

/**
 * 物品皮肤
 *
 * Records the skin of an item.
 */
class ItemSkin(
    /**
     * Skin identifier.
     */
    var id: String,
    /**
     * Skin owner.
     */
    var owner: UUID,
) : ItemAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "item_skin")
    }

    override fun asAttributeModifier(): AttributeModifier {
        throw UnsupportedOperationException()
    }

    // String

    override fun save(): BinaryTag {
        return compoundBinaryTag {
            putString("id", id)
            putLongArray("owner", longArrayOf(owner.mostSignificantBits, owner.leastSignificantBits))
        }
    }

    override fun load(tag: BinaryTag) {
        if (tag is CompoundBinaryTag) {
            id = tag.getString("id")
            owner = tag.getLongArray("owner").let { UUID(it[0], it[1]) }
        }
    }
}
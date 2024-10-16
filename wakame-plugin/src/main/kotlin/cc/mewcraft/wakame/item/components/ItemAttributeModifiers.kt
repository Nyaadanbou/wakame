package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.*
import com.google.common.collect.ImmutableMultimap
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag


data class ItemAttributeModifiers(
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ItemAttributeModifiers> {
        override fun codec(id: String): ItemComponentType<ItemAttributeModifiers> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemAttributeModifiers> {
        override fun read(holder: ItemComponentHolder): ItemAttributeModifiers? {
            val im = holder.item.itemMeta ?: return null
            if (im.hasAttributeModifiers()) {
                val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                return ItemAttributeModifiers(showInTooltip)
            }
            return null
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.attributeModifiers = null
            }
        }

        override fun write(holder: ItemComponentHolder, value: ItemAttributeModifiers) {
            holder.item.editMeta {
                it.attributeModifiers = ImmutableMultimap.of()
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                }
            }
        }
    }
}
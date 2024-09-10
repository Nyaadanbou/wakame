package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ShownInTooltip
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.Color
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.LeatherArmorMeta


data class ItemDyeColor(
    val rgb: Int,
    override val showInTooltip: Boolean,
) : Examinable, ShownInTooltip {

    companion object : ItemComponentBridge<ItemDyeColor> {
        override fun codec(id: String): ItemComponentType<ItemDyeColor> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemDyeColor> {
        override fun read(holder: ItemComponentHolder): ItemDyeColor? {
            val im = holder.item.itemMeta as? LeatherArmorMeta ?: return null
            val rgb = im.color.asRGB()
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_DYE)
            return ItemDyeColor(rgb, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemDyeColor) {
            holder.item.editMeta<LeatherArmorMeta> {
                it.setColor(Color.fromRGB(value.rgb))
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_DYE)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_DYE)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<LeatherArmorMeta> {
                it.setColor(null)
            }
        }
    }
}

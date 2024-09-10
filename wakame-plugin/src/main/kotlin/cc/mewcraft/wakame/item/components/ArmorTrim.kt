package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern


data class ArmorTrim(
    val pattern: TrimPattern,
    val material: TrimMaterial,
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ArmorTrim> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.TRIM)

        override fun codec(id: String): ItemComponentType<ArmorTrim> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ArmorTrim> {
        override fun read(holder: ItemComponentHolder): ArmorTrim? {
            val im = holder.item.itemMeta as? ArmorMeta ?: return null
            val trim = im.trim ?: return null

            val pattern = trim.pattern
            val material = trim.material
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_ARMOR_TRIM)

            return ArmorTrim(pattern, material, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ArmorTrim) {
            holder.item.editMeta<ArmorMeta> {
                it.trim = org.bukkit.inventory.meta.trim.ArmorTrim(value.material, value.pattern)
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_ARMOR_TRIM)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<ArmorMeta> {
                it.trim = null
            }
        }
    }
}
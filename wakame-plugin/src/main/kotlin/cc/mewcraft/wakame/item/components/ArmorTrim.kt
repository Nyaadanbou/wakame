package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemArmorTrim
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import org.bukkit.inventory.meta.trim.ArmorTrim as BukkitArmorTrim


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
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: ArmorTrim) {
            val bukkitArmorTrim = BukkitArmorTrim(value.material, value.pattern)
            val paperArmorTrim = ItemArmorTrim.itemArmorTrim(bukkitArmorTrim, value.showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.TRIM, paperArmorTrim)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}
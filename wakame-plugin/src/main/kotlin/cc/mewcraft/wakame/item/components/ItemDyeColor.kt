package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.kyori.examination.Examinable
import org.bukkit.Color


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
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: ItemDyeColor) {
            val paperDyedItemColor = DyedItemColor.dyedItemColor(Color.fromRGB(value.rgb), value.showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.DYED_COLOR, paperDyedItemColor)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}

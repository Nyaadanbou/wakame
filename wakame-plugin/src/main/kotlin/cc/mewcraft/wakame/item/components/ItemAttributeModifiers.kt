package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.examination.Examinable
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers as PaperItemAttributeModifiers


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
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: ItemAttributeModifiers) {
            val showInTooltip = value.showInTooltip
            val paperItemAttributeModifiers = PaperItemAttributeModifiers.itemAttributes().showInTooltip(showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, paperItemAttributeModifiers)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}
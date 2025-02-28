package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.examination.Examinable
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate as PaperItemAdventurePredicate


data class ItemAdventurePredicate(
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ItemAdventurePredicate> {
        override fun codec(id: String): ItemComponentType<ItemAdventurePredicate> {
            return when (id) {
                ItemConstants.CAN_BREAK -> CodecForCanBreak(id)
                ItemConstants.CAN_PLACE_ON -> CodecForCanPlaceOn(id)
                else -> throw IllegalArgumentException("Unknown codec id: '$id'")
            }
        }
    }

    private data class CodecForCanBreak(
        override val id: String,
    ) : ItemComponentType<ItemAdventurePredicate> {
        companion object {
            /**
             * 该组件的配置文件.
             */
            private val config = ItemComponentConfig.provide(ItemConstants.CAN_BREAK)
        }

        override fun read(holder: ItemComponentHolder): ItemAdventurePredicate? {
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: ItemAdventurePredicate) {
            val showInTooltip = value.showInTooltip
            val paperItemAdventurePredicate = PaperItemAdventurePredicate.itemAdventurePredicate().showInTooltip(showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.CAN_BREAK, paperItemAdventurePredicate)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }

    private data class CodecForCanPlaceOn(
        override val id: String,
    ) : ItemComponentType<ItemAdventurePredicate> {
        companion object {
            /**
             * 该组件的配置文件.
             */
            private val config = ItemComponentConfig.provide(ItemConstants.CAN_PLACE_ON)
        }

        override fun read(holder: ItemComponentHolder): ItemAdventurePredicate? {
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: ItemAdventurePredicate) {
            val showInTooltip = value.showInTooltip
            val paperItemAdventurePredicate = PaperItemAdventurePredicate.itemAdventurePredicate().showInTooltip(showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.CAN_PLACE_ON, paperItemAdventurePredicate)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}
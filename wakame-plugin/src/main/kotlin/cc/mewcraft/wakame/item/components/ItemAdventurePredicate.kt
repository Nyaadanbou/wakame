package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag


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
            val im = holder.item.itemMeta ?: return null
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_DESTROYS)
            return ItemAdventurePredicate(showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemAdventurePredicate) {
            holder.item.editMeta { im ->
                if (value.showInTooltip) {
                    im.removeItemFlags(ItemFlag.HIDE_DESTROYS)
                } else {
                    im.addItemFlags(ItemFlag.HIDE_DESTROYS)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { im ->
                im.removeItemFlags(ItemFlag.HIDE_DESTROYS)
            }
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
            val im = holder.item.itemMeta ?: return null
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_PLACED_ON)
            return ItemAdventurePredicate(showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemAdventurePredicate) {
            holder.item.editMeta { im ->
                if (value.showInTooltip) {
                    im.removeItemFlags(ItemFlag.HIDE_PLACED_ON)
                } else {
                    im.addItemFlags(ItemFlag.HIDE_PLACED_ON)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { im ->
                im.removeItemFlags(ItemFlag.HIDE_PLACED_ON)
            }
        }
    }
}
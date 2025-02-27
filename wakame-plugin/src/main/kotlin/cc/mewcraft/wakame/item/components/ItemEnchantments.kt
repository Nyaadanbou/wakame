package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.examination.Examinable
import org.bukkit.enchantments.Enchantment
import io.papermc.paper.datacomponent.item.ItemEnchantments as PaperItemEnchantments


data class ItemEnchantments(
    val enchantments: Map<Enchantment, Int>,
    override val showInTooltip: Boolean,
) : Examinable, ShownInTooltip {
    companion object : ItemComponentBridge<ItemEnchantments> {
        override fun codec(id: String): ItemComponentType<ItemEnchantments> {
            return when (id) {
                ItemConstants.ENCHANTMENTS -> CodecForEnchantments(id)
                ItemConstants.STORED_ENCHANTMENTS -> CodecForStoredEnchantments(id)
                else -> throw IllegalArgumentException("Unknown codec id: '$id'")
            }
        }
    }

    private data class CodecForEnchantments(
        override val id: String,
    ) : ItemComponentType<ItemEnchantments> {
        override fun read(holder: ItemComponentHolder): ItemEnchantments? {
            val enchantments = holder.bukkitStack.getData(DataComponentTypes.ENCHANTMENTS) ?: return null
            return ItemEnchantments(enchantments.enchantments(), enchantments.showInTooltip())
        }

        override fun write(holder: ItemComponentHolder, value: ItemEnchantments) {
            val enchantments = value.enchantments
            val showInTooltip = value.showInTooltip
            val paperItemEnchantments = PaperItemEnchantments.itemEnchantments(enchantments, showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.ENCHANTMENTS, paperItemEnchantments)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }

    private data class CodecForStoredEnchantments(
        override val id: String,
    ) : ItemComponentType<ItemEnchantments> {
        override fun read(holder: ItemComponentHolder): ItemEnchantments? {
            val enchantments = holder.bukkitStack.getData(DataComponentTypes.ENCHANTMENTS) ?: return null
            return ItemEnchantments(enchantments.enchantments(), enchantments.showInTooltip())
        }

        override fun write(holder: ItemComponentHolder, value: ItemEnchantments) {
            val enchantments = value.enchantments
            val showInTooltip = value.showInTooltip
            val paperItemEnchantments = PaperItemEnchantments.itemEnchantments(enchantments, showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.STORED_ENCHANTMENTS, paperItemEnchantments)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}
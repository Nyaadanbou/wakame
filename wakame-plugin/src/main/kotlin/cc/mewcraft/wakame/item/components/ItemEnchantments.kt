package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ShownInTooltip
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.EnchantmentStorageMeta


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
            val im = holder.item.itemMeta ?: return null
            val enchantments = im.enchants.takeIf { it.isNotEmpty() } ?: return null
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_ENCHANTS)
            return ItemEnchantments(enchantments, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemEnchantments) {
            holder.item.editMeta {
                for ((enchantment, level) in value.enchantments) {
                    it.addEnchant(enchantment, level, true)
                }
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.removeEnchantments()
        }
    }

    private data class CodecForStoredEnchantments(
        override val id: String,
    ) : ItemComponentType<ItemEnchantments> {
        override fun read(holder: ItemComponentHolder): ItemEnchantments? {
            val im = holder.item.itemMeta as? EnchantmentStorageMeta ?: return null
            val enchantments = im.enchants.takeIf { it.isNotEmpty() } ?: return null
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_STORED_ENCHANTS)
            return ItemEnchantments(enchantments, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemEnchantments) {
            holder.item.editMeta<EnchantmentStorageMeta> {
                for ((storedEnchant, _) in it.storedEnchants) {
                    it.removeStoredEnchant(storedEnchant)
                }
                for ((enchantment, level) in value.enchantments) {
                    it.addStoredEnchant(enchantment, level, true)
                }
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_STORED_ENCHANTS)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<EnchantmentStorageMeta> {
                for ((storedEnchant, _) in it.storedEnchants) {
                    it.removeStoredEnchant(storedEnchant)
                }
            }
        }
    }
}
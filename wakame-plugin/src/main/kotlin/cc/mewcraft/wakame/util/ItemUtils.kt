package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.inventory.ShadowCraftMetaItem
import me.lucko.helper.shadows.nbt.ShadowTag
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

val ItemMeta.unhandledTags: MutableMap<String, ShadowTag>
    get() = BukkitShadowFactory.global().shadow<ShadowCraftMetaItem>(this).unhandledTags()

/**
 * Gets the custom model data or `0`, if it does not exist.
 */
val ItemStack.modelData: Int
    get() {
        if (hasItemMeta()) {
            val itemMeta = itemMeta!!
            if (itemMeta.hasCustomModelData()) return itemMeta.customModelData
        }

        return 0
    }

/**
 * Gets the display name or an empty text, if the display name does not
 * exist.
 */
val ItemStack.adventureName: Component
    get() {
        if (this.hasItemMeta()) {
            return this.itemMeta.displayName() ?: Component.empty()
        }
        return Component.empty()
    }

/**
 * Get the lore or an empty list, if the lore does not exist.
 */
val ItemStack.adventureLore: List<Component>
    get() {
        if (this.hasItemMeta()) {
            return this.itemMeta.lore() ?: emptyList()
        }
        return emptyList()
    }

//<editor-fold desc="Adventure NBT is for test only">
fun ItemStack.getNbt(): CompoundBinaryTag =
    ItemStackAdventureNbt.getNbt(this)

fun ItemStack.getNbtOrNull(): CompoundBinaryTag? =
    ItemStackAdventureNbt.getNbtOrNull(this)

fun ItemStack.setNbt(compound: CompoundBinaryTag.Builder.() -> Unit) =
    ItemStackAdventureNbt.setNbt(this, compound)

fun ItemStack.copyWriteNbt(compound: CompoundBinaryTag.Builder.() -> Unit): ItemStack =
    ItemStackAdventureNbt.copyWriteNbt(this, compound)
//</editor-fold>

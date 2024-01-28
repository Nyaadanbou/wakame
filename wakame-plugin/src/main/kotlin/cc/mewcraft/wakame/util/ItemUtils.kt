package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.inventory.ShadowCraftMetaItem
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

val ItemStack.customModelData: Int
    get() {
        if (hasItemMeta()) {
            val itemMeta = itemMeta!!
            if (itemMeta.hasCustomModelData()) return itemMeta.customModelData
        }

        return 0
    }

/**
 * Gets the display name or [Component.empty], if the display name does not
 * already exist.
 */
val ItemStack.adventureName: Component
    get() {
        if (this.hasItemMeta()) {
            return this.itemMeta.displayName() ?: Component.empty()
        }
        return Component.empty()
    }

/**
 * Get the lore or [emptyList], if the lore does not already exist.
 */
val ItemStack.adventureLore: List<Component>
    get() {
        if (this.hasItemMeta()) {
            return this.itemMeta.lore() ?: emptyList()
        }
        return emptyList()
    }

val ItemMeta.unhandledTags: MutableMap<String, ShadowTag>
    get() = BukkitShadowFactory.global().shadow<ShadowCraftMetaItem>(this).unhandledTags()

var ItemStack.wakameCompound: CompoundShadowTag
    get() = ItemStackShadowNbt.getWakameCompound(this)
    set(value) = ItemStackShadowNbt.setWakameCompound(this, value)

val ItemStack.wakameCompoundOrNull: CompoundShadowTag?
    get() = ItemStackShadowNbt.getWakameCompoundOrNull(this)

////// Not so useful

fun ItemStack.getNbt(): CompoundBinaryTag =
    ItemStackAdventureNbt.getNbt(this)

fun ItemStack.getNbtOrNull(): CompoundBinaryTag? =
    ItemStackAdventureNbt.getNbtOrNull(this)

fun ItemStack.setNbt(compound: CompoundBinaryTag.Builder.() -> Unit) =
    ItemStackAdventureNbt.setNbt(this, compound)

fun ItemStack.copyWriteNbt(compound: CompoundBinaryTag.Builder.() -> Unit): ItemStack =
    ItemStackAdventureNbt.copyWriteNbt(this, compound)

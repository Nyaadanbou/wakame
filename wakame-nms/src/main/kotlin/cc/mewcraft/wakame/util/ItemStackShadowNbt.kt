package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.inventory.ShadowCraftItemStack0
import cc.mewcraft.wakame.shadow.inventory.ShadowCraftMetaItem0
import cc.mewcraft.wakame.shadow.inventory.ShadowItemStack
import me.lucko.helper.shadows.nbt2.CompoundShadowTag
import me.lucko.helper.shadows.nbt2.ShadowTag
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.nbt.BinaryTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import net.minecraft.world.item.ItemStack as MojangStack

/**
 * Reads/writes the NBT tags from items as [BinaryTag].
 */
object ItemStackShadowNbt {
    fun getWakameCompound(itemStack: ItemStack): CompoundShadowTag {
        val handle = itemStack.handle
        if (handle != null) { // CraftItemStack
            return handle.wakameCompound
        } else { // strictly-Bukkit ItemStack
            val unhandledTags = itemStack.backingItemMeta!!.unhandledTags
            val tag = unhandledTags.getOrPut("wakame", ::CompoundTag) as CompoundTag
            return tag.wrap
        }
    }

    fun setWakameCompound(itemStack: ItemStack, value: CompoundShadowTag) {
        val handle = itemStack.handle
        if (handle != null) { // CraftItemStack
            handle.wakameCompound = value
        } else { // strictly-Bukkit ItemStack
            itemStack.backingItemMeta!!.unhandledTags["wakame"] = value.unwrap
        }
    }

    fun getWakameCompoundOrNull(itemStack: ItemStack): CompoundShadowTag? {
        val handle = itemStack.handle
        return if (handle != null) { // CraftItemStack
            handle.wakameCompoundOrNull
        } else { // strictly-Bukkit ItemStack
            (itemStack.backingItemMeta?.unhandledTags?.get("wakame") as? CompoundTag)?.wrap
        }
    }
}

////// Shadow un(wrappers) //////

internal val Tag.wrap: ShadowTag
    get() = BukkitShadowFactory.global().shadow<ShadowTag>(this)
internal val ShadowTag.unwrap: Tag
    get() = this.shadowTarget as Tag
internal val CompoundTag.wrap: CompoundShadowTag
    get() = BukkitShadowFactory.global().shadow<CompoundShadowTag>(this)
internal val CompoundShadowTag.unwrap: CompoundTag
    get() = this.shadowTarget as CompoundTag

//////

internal val ItemMeta.unhandledTags: MutableMap<String, Tag>
    get() = BukkitShadowFactory.global().shadow<ShadowCraftMetaItem0>(this).getUnhandledTags()

////// MojangStack - Wakame Compound

internal var MojangStack.wakameCompound: CompoundShadowTag
    get() {
        val compoundTag = this.orCreateTag.getOrPut("wakame") { CompoundTag() }
        return compoundTag.wrap
    }
    set(value) {
        this.orCreateTag.put("wakame", value.unwrap)
    }

internal val MojangStack.wakameCompoundOrNull: CompoundShadowTag?
    get() = this.tag?.getCompoundOrNull("wakame")?.wrap

////// BukkitStack - Wakame Compound

// moved to singleton object - the gradle somehow doesn't know the re-obfuscated extensions functions

////// Internals //////

internal val ItemStack.backingItemMeta: ItemMeta?
    get() {
        val shadow = BukkitShadowFactory.global().shadow<ShadowItemStack>(this)
        var backingMeta = shadow.getMeta()

        if (backingMeta == null) {
            backingMeta = Bukkit.getItemFactory().getItemMeta(type)
            shadow.setMeta(backingMeta)
        }

        return backingMeta
    }

internal val ItemStack.handle: MojangStack?
    get() {
        val shadow = BukkitShadowFactory.global().shadow<ShadowCraftItemStack0>(this)
        val shadowTarget = shadow.shadowTarget!!
        if (shadowTarget::class.isInstance(this)) { // Use shadow to avoid versioned CB package import
            return shadow.getHandle()
        }

        return null
    }
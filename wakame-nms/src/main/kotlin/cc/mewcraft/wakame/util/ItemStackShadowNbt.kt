@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.shadow.inventory.ShadowCraftItemStack0
import cc.mewcraft.wakame.shadow.inventory.ShadowCraftMetaItem0
import cc.mewcraft.wakame.shadow.inventory.ShadowItemStack
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import me.lucko.shadow.targetClass
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import net.minecraft.world.item.ItemStack as MojangStack

private const val ROOT_COMPOUND_NAME: String = NekoTags.ROOT

//<editor-fold desc="Shadow un(wrappers)">
internal val Tag.wrap: ShadowTag
    get() = BukkitShadowFactory.global().shadow<ShadowTag>(this)
internal val ShadowTag.unwrap: Tag
    get() = this.shadowTarget as Tag
internal val CompoundTag.wrap: CompoundShadowTag
    get() = BukkitShadowFactory.global().shadow<CompoundShadowTag>(this)
internal val CompoundShadowTag.unwrap: CompoundTag
    get() = this.shadowTarget as CompoundTag
//</editor-fold>

internal val ItemMeta.unhandledTags: MutableMap<String, Tag>
    get() = BukkitShadowFactory.global().shadow<ShadowCraftMetaItem0>(this).getUnhandledTags()

//<editor-fold desc="CraftItemStack - Direct Access to `tags.display`">
/**
 * Sets the display name through JSON string. You may pass a `null` to
 * remove the name. This function will directly write the given JSON string
 * to the NBT tag, so make sure that you pass a valid JSON string, or else
 * the server will throw.
 *
 * Only works if `this` [ItemStack] is NMS-object backed.
 */
var ItemStack.backingDisplayName: String?
    get() = throw UnsupportedOperationException("Get operation is not supported")
    set(value) {
        if (value != null) {
            this.handle?.getOrCreateTagElement("display")?.putString("Name", value)
        } else {
            this.handle?.getTagElement("display")?.remove("Name")
        }
    }

/**
 * Sets the lore directly through JSON string. You may pass a `null` to
 * remove the lore. This function will directly write the given JSON string
 * list to the NBT tag, so make sure that you pass a valid JSON string, or
 * else the server will throw.
 *
 * Only works if `this` [ItemStack] is NMS-object backed.
 */
var ItemStack.backingLore: List<String>?
    get() = throw UnsupportedOperationException("Get operation is not supported")
    set(value) {
        if (value != null) {
            this.handle?.getOrCreateTagElement("display")?.put("Lore", NmsNbtUtils.createStringList(value))
        } else {
            this.handle?.getTagElement("display")?.remove("Lore")
        }
    }

/**
 * Sets the custom model data.
 * You may pass a `null` to remove the custom model data.
 * This function will directly write the given integer to the NBT tag.
 *
 * Only works if `this` [ItemStack] is NMS-object backed.
 */
var ItemStack.backingCustomModelData: Int?
    get() = throw UnsupportedOperationException("Get operation is not supported")
    set(value) {
        if (value != null) {
            this.handle?.tag?.putInt("CustomModelData", value)
        } else {
            this.handle?.tag?.remove("CustomModelData")
        }
    }
//</editor-fold>

//<editor-fold desc="MojangStack - Neko Compound">
internal var MojangStack.nekoCompound: CompoundShadowTag
    get() {
        val compoundTag = this.orCreateTag.getOrPut(ROOT_COMPOUND_NAME) { CompoundTag() }
        return compoundTag.wrap
    }
    set(value) {
        this.orCreateTag.put(ROOT_COMPOUND_NAME, value.unwrap)
    }

internal val MojangStack.nekoCompoundOrNull: CompoundShadowTag?
    get() = this.tag?.getCompoundOrNull(ROOT_COMPOUND_NAME)?.wrap
//</editor-fold>

//<editor-fold desc="BukkitStack - Neko Compound">
/**
 * ## Getter
 *
 * Gets the [ROOT_COMPOUND_NAME] compound tag from `this`. If the
 * [ROOT_COMPOUND_NAME] compound tag does not already exist, a new compound
 * tag will be created and **saved** to `this`.
 *
 * ## Setter
 *
 * Sets the [ROOT_COMPOUND_NAME] compound tag of `this` to given
 * value, overwriting any existing [ROOT_COMPOUND_NAME] compound tag
 * on the `this`.
 *
 * You can also set `this` to `null` to removes the [ROOT_COMPOUND_NAME]
 * compound tag from `this`.
 */
var ItemStack.nekoCompound: CompoundShadowTag
    get() {
        val handle = this.handle
        if (handle != null) { // CraftItemStack
            return handle.nekoCompound
        } else { // strictly-Bukkit ItemStack
            val unhandledTags = this.backingItemMeta!!.unhandledTags
            val tag = unhandledTags.getOrPut(ROOT_COMPOUND_NAME, ::CompoundTag) as CompoundTag
            return tag.wrap
        }
    }
    set(value) {
        val handle = this.handle
        if (handle != null) { // CraftItemStack
            handle.nekoCompound = value
        } else { // strictly-Bukkit ItemStack
            this.backingItemMeta!!.unhandledTags[ROOT_COMPOUND_NAME] = value.unwrap
        }
    }

/**
 * Gets the [ROOT_COMPOUND_NAME] compound tag from `this` or `null`, if it does
 * not exist. Unlike [ItemStack.nekoCompound], which possibly modifies the
 * item's NBT tags, this function will not modify `this` at all.
 */
val ItemStack.nekoCompoundOrNull: CompoundShadowTag?
    get() {
        val handle = this.handle
        return if (handle != null) { // CraftItemStack
            handle.nekoCompoundOrNull
        } else { // strictly-Bukkit ItemStack
            (this.backingItemMeta?.unhandledTags?.get(ROOT_COMPOUND_NAME) as? CompoundTag)?.wrap
        }
    }

/**
 * Removes the [ROOT_COMPOUND_NAME] compound tag from `this`.
 */
fun ItemStack.removeNekoCompound() {
    val handle = this.handle
    if (handle != null) { // CraftItemStack
        handle.removeTagKey(NekoTags.ROOT)
    } else { // strictly-Bukkit ItemStack
        this.backingItemMeta?.unhandledTags?.remove(NekoTags.ROOT)
    }
}
//</editor-fold>

/**
 * Gets the [ItemMeta] without cloning. If the item meta does not already
 * exist, it will try to create one and then return.
 */
val ItemStack.backingItemMeta: ItemMeta?
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
        val obcClass = BukkitShadowFactory.global().targetClass<ShadowCraftItemStack0>()
        if (obcClass.isInstance(this)) { // Use shadow to avoid versioned CB package import
            val shadow = BukkitShadowFactory.global().shadow<ShadowCraftItemStack0>(this)
            return shadow.getHandle()
        }

        return null
    }

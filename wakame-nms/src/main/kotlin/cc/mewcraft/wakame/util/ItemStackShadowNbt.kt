@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.inventory.ShadowCraftMetaItem0
import cc.mewcraft.wakame.shadow.inventory.ShadowItemStack
import io.papermc.paper.adventure.PaperAdventure
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import net.minecraft.world.item.ItemStack as MojangStack

const val WAKAME_COMPOUND_NAME: String = "wakame"

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

internal val ItemMeta.customTag: CompoundTag
    get() = BukkitShadowFactory.global().shadow<ShadowCraftMetaItem0>(this).getCustomTag()

//<editor-fold desc="CraftItemStack - Direct Access to `tags.display`">
/**
 * Sets the custom name. You may pass a `null` to remove the name.
 *
 * Only works if `this` [ItemStack] is NMS-object backed.
 */
var ItemStack.backingCustomName: Component?
    get() {
        return PaperAdventure.asAdventure(this.handle?.get(DataComponents.CUSTOM_NAME))
    }
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.CUSTOM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle?.remove(DataComponents.CUSTOM_NAME)
        }
    }

/**
 * Sets the item name. You may pass a `null` to remove the name.
 *
 * Only works if `this` [ItemStack] is NMS-object backed.
 */
var ItemStack.backingItemName: Component?
    get() {
        return PaperAdventure.asAdventure(this.handle?.get(DataComponents.CUSTOM_NAME))
    }
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.CUSTOM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle?.remove(DataComponents.CUSTOM_NAME)
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
var ItemStack.backingLore: List<Component>?
    get() {
        return this.handle?.get(DataComponents.LORE)?.lines?.map(PaperAdventure::asAdventure)
    }
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.LORE, ItemLore(value.map(PaperAdventure::asVanilla)))
        } else {
            this.handle?.remove(DataComponents.LORE)
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
    get() {
        return this.handle?.get(DataComponents.CUSTOM_MODEL_DATA)?.value
    }
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(value))
        } else {
            this.handle?.remove(DataComponents.CUSTOM_MODEL_DATA)
        }
    }
//</editor-fold>

private fun MojangStack.getCustomData(): CompoundTag? {
    val customData = this.get(DataComponents.CUSTOM_DATA)

    @Suppress("DEPRECATION")
    val compoundTag = customData?.unsafe // this returns the backing CompoundTag for us
    return compoundTag
}

private fun MojangStack.getCustomDataOrCreate(): CompoundTag {
    val customData = this.get(DataComponents.CUSTOM_DATA) ?: run {
        val empty = CustomData.of(CompoundTag())
        this.set(DataComponents.CUSTOM_DATA, empty)
        return@run empty
    }

    @Suppress("DEPRECATION")
    val compoundTag = customData.unsafe // this returns the backing CompoundTag for us
    return compoundTag
}

//<editor-fold desc="MojangStack - Neko Compound">
internal var MojangStack.nekoCompound: CompoundShadowTag
    get() {
        val customData = this.getCustomDataOrCreate()
        val wakameTag = customData.getOrPut(WAKAME_COMPOUND_NAME, ::CompoundTag)
        return wakameTag.wrap
    }
    set(value) {
        val customData = this.getCustomDataOrCreate()
        customData.put(WAKAME_COMPOUND_NAME, value.unwrap)
    }

internal val MojangStack.nekoCompoundOrNull: CompoundShadowTag?
    get() {
        val customData = this.getCustomData()
        val wakameTag = customData?.getCompoundOrNull(WAKAME_COMPOUND_NAME)
        return wakameTag?.wrap
    }
//</editor-fold>

//<editor-fold desc="BukkitStack - Neko Compound">
/**
 * ## Getter
 *
 * Gets the [WAKAME_COMPOUND_NAME] compound tag from `this`. If the
 * [WAKAME_COMPOUND_NAME] compound tag does not already exist, a new compound
 * tag will be created and **saved** to `this`.
 *
 * ## Setter
 *
 * Sets the [WAKAME_COMPOUND_NAME] compound tag of `this` to given
 * value, overwriting any existing [WAKAME_COMPOUND_NAME] compound tag
 * on the `this`.
 *
 * You can also set `this` to `null` to removes the [WAKAME_COMPOUND_NAME]
 * compound tag from `this`.
 */
var ItemStack.nekoCompound: CompoundShadowTag
    get() {
        val handle = this.handle
        if (handle != null) { // CraftItemStack
            return handle.nekoCompound
        } else { // strictly-Bukkit ItemStack
            val customTag = this.backingItemMeta!!.customTag
            val tag = customTag.getOrPut(WAKAME_COMPOUND_NAME, ::CompoundTag) as CompoundTag
            return tag.wrap
        }
    }
    set(value) {
        val handle = this.handle
        if (handle != null) { // CraftItemStack
            handle.nekoCompound = value
        } else { // strictly-Bukkit ItemStack
            this.backingItemMeta!!.customTag.put(WAKAME_COMPOUND_NAME, value.unwrap)
        }
    }

/**
 * Gets the [WAKAME_COMPOUND_NAME] compound tag from `this` or `null`, if it does
 * not exist. Unlike [ItemStack.nekoCompound], which possibly modifies the
 * item's NBT tags, this function will not modify `this` at all.
 */
val ItemStack.nekoCompoundOrNull: CompoundShadowTag?
    get() {
        val handle = this.handle
        return if (handle != null) { // CraftItemStack
            handle.nekoCompoundOrNull
        } else { // strictly-Bukkit ItemStack
            (this.backingItemMeta?.customTag?.get(WAKAME_COMPOUND_NAME) as? CompoundTag)?.wrap
        }
    }

/**
 * Removes the [WAKAME_COMPOUND_NAME] compound tag from `this`.
 */
fun ItemStack.removeNekoCompound() {
    val handle = this.handle
    if (handle != null) { // CraftItemStack
        handle.getCustomData()?.remove(WAKAME_COMPOUND_NAME)
    } else { // strictly-Bukkit ItemStack
        this.backingItemMeta?.customTag?.remove(WAKAME_COMPOUND_NAME)
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
    get() = (this as? CraftItemStack)?.handle

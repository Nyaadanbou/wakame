@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.inventory.ShadowCraftMetaItem0
import cc.mewcraft.wakame.shadow.inventory.ShadowItemStack
import io.papermc.paper.adventure.PaperAdventure
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.meta.ItemMeta
import cc.mewcraft.nbt.CompoundTag as CompoundShadowTag
import cc.mewcraft.nbt.Tag as ShadowTag
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

const val WAKAME_TAG_NAME: String = "wakame"

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

//<editor-fold desc="BukkitStack (NMS)">
private val BukkitStack.handle: MojangStack?
    get() = (this as? CraftItemStack)?.handle

/**
 * Sets the custom name. You may pass a `null` to remove it.
 *
 * Only works if `this` [BukkitStack] is NMS-object backed.
 */
var BukkitStack.backingCustomName: Component?
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
 * Sets the item name. You may pass a `null` to remove it.
 *
 * Only works if `this` [BukkitStack] is NMS-object backed.
 */
var BukkitStack.backingItemName: Component?
    get() {
        return PaperAdventure.asAdventure(this.handle?.get(DataComponents.ITEM_NAME))
    }
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.ITEM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle?.remove(DataComponents.ITEM_NAME)
        }
    }

/**
 * Sets the item lore. You may pass a `null` to remove it.
 *
 * Only works if `this` [BukkitStack] is NMS-object backed.
 */
var BukkitStack.backingLore: List<Component>?
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
 * Sets the custom model data. You may pass a `null` to remove it.
 *
 * Only works if `this` [BukkitStack] is NMS-object backed.
 */
var BukkitStack.backingCustomModelData: Int?
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

//<editor-fold desc="BukkitStack (API & NMS) - Access to wakame tag">
/**
 * ## Getter
 *
 * Gets the [WAKAME_TAG_NAME] compound tag from `this`. If the
 * [WAKAME_TAG_NAME] compound tag does not already exist, a new compound
 * tag will be created and **saved** to `this`.
 *
 * ## Setter
 *
 * Sets the [WAKAME_TAG_NAME] compound tag of `this` to given
 * value, overwriting any existing [WAKAME_TAG_NAME] compound tag
 * on the `this`.
 *
 * You can also set `this` to `null` to removes the [WAKAME_TAG_NAME]
 * compound tag from `this`.
 */
var BukkitStack.wakameTag: CompoundShadowTag
    get() {
        val handle = this.handle
        if (handle != null) { // CraftItemStack
            return handle.wakameTag
        } else { // strictly-Bukkit ItemStack
            val customTag = this.backingItemMeta!!.getDirectCustomData()
            val tag = customTag.getOrPut(WAKAME_TAG_NAME, ::CompoundTag) as CompoundTag
            return tag.wrap
        }
    }
    set(value) {
        val handle = this.handle
        if (handle != null) { // CraftItemStack
            handle.wakameTag = value
        } else { // strictly-Bukkit ItemStack
            this.backingItemMeta!!.getDirectCustomData().put(WAKAME_TAG_NAME, value.unwrap)
        }
    }

/**
 * Gets the [WAKAME_TAG_NAME] compound tag from `this` or `null`, if it does
 * not exist. Unlike [BukkitStack.wakameTag], which possibly modifies the
 * item's NBT tags, this function will not modify `this` at all.
 */
val BukkitStack.wakameTagOrNull: CompoundShadowTag?
    get() {
        val handle = this.handle
        return if (handle != null) { // CraftItemStack
            handle.wakameTagOrNull
        } else { // strictly-Bukkit ItemStack
            (this.backingItemMeta?.getDirectCustomData()?.get(WAKAME_TAG_NAME) as? CompoundTag)?.wrap
        }
    }

/**
 * Removes the [WAKAME_TAG_NAME] compound tag from `this`.
 */
fun BukkitStack.removeWakameTag() {
    val handle = this.handle
    if (handle != null) { // CraftItemStack
        handle.getDirectCustomData()?.remove(WAKAME_TAG_NAME)
    } else { // strictly-Bukkit ItemStack
        this.backingItemMeta?.getDirectCustomData()?.remove(WAKAME_TAG_NAME)
    }
}
//</editor-fold>

//<editor-fold desc="BukkitStack (API) - Access to ItemMeta on strictly-Bukkit stacks">
/**
 * Gets the [ItemMeta] on strictly-Bukkit [BukkitStack] without cloning.
 * If the item meta does not already exist, it will try to create one
 * and then return.
 */
private val BukkitStack.backingItemMeta: ItemMeta?
    get() {
        if (this is CraftItemStack)
            return null

        val shadow = BukkitShadowFactory.global().shadow<ShadowItemStack>(this)
        var backingMeta = shadow.getMeta()

        if (backingMeta == null) {
            backingMeta = Bukkit.getItemFactory().getItemMeta(type)
            shadow.setMeta(backingMeta)
        }

        return backingMeta
    }
//</editor-fold>

//<editor-fold desc="ItemMeta">
/**
 * Access to the `minecraft:custom_data` on [ItemMeta].
 */
private fun ItemMeta.getDirectCustomData(): CompoundTag {
    val shadow = BukkitShadowFactory.global().shadow<ShadowCraftMetaItem0>(this)
    val customTag = shadow.getCustomTag()
    if (customTag === null) {
        val newCustomTag = CompoundTag()
        shadow.setCustomTag(newCustomTag)
        return newCustomTag
    }
    return customTag
}
//</editor-fold>

//<editor-fold desc="MojangStack">
private var MojangStack.wakameTag: CompoundShadowTag
    get() {
        val customData = this.getDirectCustomDataOrCreate()
        val wakameTag = customData.getOrPut(WAKAME_TAG_NAME, ::CompoundTag)
        return wakameTag.wrap
    }
    set(value) {
        // 替换整个物品组件: minecraft:custom_data
        // 也就是说一旦该 setter 函数被调用, 那么物品组件
        // minecraft:custom_data 中的其他内容都会被移除.
        val tag = CompoundTag()
        tag.put(WAKAME_TAG_NAME, value.unwrap)
        this.setCustomData(tag)
    }

private val MojangStack.wakameTagOrNull: CompoundShadowTag?
    get() {
        val customData = this.getDirectCustomData()
        val wakameTag = customData?.getCompoundOrNull(WAKAME_TAG_NAME)
        return wakameTag?.wrap
    }
//</editor-fold>
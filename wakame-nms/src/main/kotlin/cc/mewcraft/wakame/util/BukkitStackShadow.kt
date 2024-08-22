@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.inventory.ShadowItemStack
import io.papermc.paper.adventure.*
import me.lucko.shadow.*
import me.lucko.shadow.bukkit.*
import net.kyori.adventure.text.*
import net.minecraft.core.component.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.component.*
import org.bukkit.craftbukkit.inventory.*
import cc.mewcraft.nbt.CompoundTag as CompoundShadowTag
import cc.mewcraft.nbt.Tag as ShadowTag
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

const val NYA_TAG_NAME: String = "wakame"

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
/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.backingCustomName: Component?
    get() = this.handle.get(DataComponents.CUSTOM_NAME)?.let(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle.set(DataComponents.CUSTOM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle.remove(DataComponents.CUSTOM_NAME)
        }
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.backingItemName: Component?
    get() = this.handle.get(DataComponents.ITEM_NAME)?.let(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle.set(DataComponents.ITEM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle.remove(DataComponents.ITEM_NAME)
        }
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.backingLore: List<Component>?
    get() = this.handle.get(DataComponents.LORE)?.lines?.map(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle.set(DataComponents.LORE, ItemLore(value.map(PaperAdventure::asVanilla)))
        } else {
            this.handle.remove(DataComponents.LORE)
        }
    }

/**
 * 设置自定义模型数据. 你可以传入 `null` 来移除它.
 */
var BukkitStack.backingCustomModelData: Int?
    get() = this.handle.get(DataComponents.CUSTOM_MODEL_DATA)?.value
    set(value) {
        if (value != null) {
            this.handle.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(value))
        } else {
            this.handle.remove(DataComponents.CUSTOM_MODEL_DATA)
        }
    }

/**
 * 获取封装的 NMS 对象.
 */
internal val BukkitStack.handle: MojangStack
    get() = if (this is CraftItemStack) {
        this.handle
    } else {
        val shadow = BukkitShadowFactory.global().shadow<ShadowItemStack>(this)
        val delegate = shadow.craftDelegate
        delegate.handle
    }
//</editor-fold>

//<editor-fold desc="BukkitStack - Access to nya tags">
var BukkitStack.nyaTag: CompoundShadowTag
    /**
     * 获取萌芽标签.
     * 如果物品上不存在萌芽标签, 将会创建一个新的萌芽标签并*写入*到物品上.
     */
    get() {
        val handle = this.handle
        return handle.nyaTag
    }
    /**
     * 设置萌芽标签.
     * 如果物品上已经存在萌芽标签, 将会覆盖掉原有的萌芽标签.
     * 你也可以传入 `null` 来移除物品上的萌芽标签.
     */
    set(value) {
        val handle = this.handle
        handle.nyaTag = value
    }

/**
 * 获取萌芽标签. 如果物品上不存在萌芽标签, 将会返回 `null`.
 * 与 [nyaTag] 不同, 该函数永远不会修改物品的任何数据.
 */
val BukkitStack.nyaTagOrNull: CompoundShadowTag?
    get() {
        val handle = this.handle
        return handle.nyaTagOrNull
    }

/**
 * 移除物品上的萌芽标签.
 */
fun BukkitStack.removeNyaTag() {
    val handle = this.handle
    handle.getUnsafeCustomData()?.remove(NYA_TAG_NAME)
}
//</editor-fold>

//<editor-fold desc="MojangStack">
private var MojangStack.nyaTag: CompoundShadowTag
    get() {
        val customData = this.getUnsafeCustomDataOrCreate()
        val nyaTag = customData.getOrPut(NYA_TAG_NAME, ::CompoundTag)
        return nyaTag.wrap
    }
    set(value) {
        // 替换整个物品组件: `minecraft:custom_data`.
        // 也就是说一旦该 setter 函数被调用, 那么物品组件
        // `minecraft:custom_data` 中的其他内容都会被移除.
        val tag = CompoundTag()
        tag.put(NYA_TAG_NAME, value.unwrap)
        this.setCustomData(tag)
    }

private val MojangStack.nyaTagOrNull: CompoundShadowTag?
    get() {
        val customData = this.getUnsafeCustomData()
        val nyaTag = customData?.getCompoundOrNull(NYA_TAG_NAME)
        return nyaTag?.wrap
    }
//</editor-fold>
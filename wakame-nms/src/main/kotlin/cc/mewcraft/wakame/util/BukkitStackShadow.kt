@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.shadow.inventory.ShadowItemStack
import io.papermc.paper.adventure.PaperAdventure
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import org.bukkit.craftbukkit.inventory.CraftItemStack
import cc.mewcraft.nbt.CompoundTag as CompoundShadowTag
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

private const val CLIENT_SIDE_KEY = "client_side"

internal val CompoundTag.wrap: CompoundShadowTag
    get() = BukkitShadowFactory.global().shadow<CompoundShadowTag>(this)
internal val CompoundShadowTag.unwrap: CompoundTag
    get() = this.shadowTarget as CompoundTag

//<editor-fold desc="BukkitStack">
/**
 * 获取封装的 NMS 对象.
 * 如果 [isEmpty] 为 `true`, 将会返回 `null`.
 * 截止至 2024/8/25, 空气不存在封装的 NMS 对象.
 */
internal val BukkitStack.handle: MojangStack?
    get() = if (this is CraftItemStack) {
        this.handle
    } else {
        BukkitShadowFactory.global().shadow<ShadowItemStack>(this).craftDelegate.handle
    }?.takeUnless(MojangStack::isEmpty)

var BukkitStack.isClientSide: Boolean
    get() = this.handle?.isClientSide ?: false
    set(value) {
        this.handle?.isClientSide = value
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.customName: Component?
    get() = this.handle?.get(DataComponents.CUSTOM_NAME)?.let(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.CUSTOM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle?.remove(DataComponents.CUSTOM_NAME)
        }
    }

@Deprecated("Use customName instead", ReplaceWith("customName"))
var BukkitStack.customName0: Component? by BukkitStack::customName

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.itemName: Component?
    get() = this.handle?.get(DataComponents.ITEM_NAME)?.let(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.ITEM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle?.remove(DataComponents.ITEM_NAME)
        }
    }

@Deprecated("Use itemName instead", ReplaceWith("itemName"))
var BukkitStack.itemName0: Component? by BukkitStack::itemName

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.lore0: List<Component>?
    get() = this.handle?.get(DataComponents.LORE)?.lines?.map(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.LORE, ItemLore(value.map(PaperAdventure::asVanilla)))
        } else {
            this.handle?.remove(DataComponents.LORE)
        }
    }

/**
 * 设置自定义模型数据. 你可以传入 `null` 来移除它.
 */
var BukkitStack.customModelData: Int?
    get() = this.handle?.get(DataComponents.CUSTOM_MODEL_DATA)?.value
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(value))
        } else {
            this.handle?.remove(DataComponents.CUSTOM_MODEL_DATA)
        }
    }

@Deprecated("Use customModelData instead", ReplaceWith("customModelData"))
var BukkitStack.customModelData0: Int? by BukkitStack::customModelData

/**
 * 设置是否隐藏附加提示.
 */
var BukkitStack.hideAdditionalTooltip: Boolean
    get() = this.handle?.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP) ?: false
    set(value) {
        this.handle?.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, if (value) net.minecraft.util.Unit.INSTANCE else null)
    }

@Deprecated("Use hideAdditionalTooltip instead", ReplaceWith("hideAdditionalTooltip"))
var BukkitStack.hideAdditionalTooltip0: Boolean by BukkitStack::hideAdditionalTooltip

/**
 * 设置是否隐藏提示.
 */
var BukkitStack.hideTooltip: Boolean
    get() = this.handle?.has(DataComponents.HIDE_TOOLTIP) ?: false
    set(value) {
        this.handle?.set(DataComponents.HIDE_TOOLTIP, if (value) net.minecraft.util.Unit.INSTANCE else null)
    }

@Deprecated("Use hideTooltip instead", ReplaceWith("hideTooltip"))
var BukkitStack.hideTooltip0: Boolean by BukkitStack::hideTooltip

/**
 * Safe read/write.
 */
var BukkitStack.nbt: CompoundShadowTag?
    get() = this.handle?.nbt
    set(value) {
        this.handle?.nbt = value
    }

/**
 * Safe read.
 */
val BukkitStack.nbtOrThrow: CompoundShadowTag
    get() = this.nbt ?: throw NoSuchElementException("No NBT present on ItemStack")

/**
 * Safe edit.
 */
fun BukkitStack.editNbt(block: (CompoundShadowTag) -> Unit) {
    this.nbt = (this.nbt ?: CompoundShadowTag.create()).apply(block)
}

/**
 * Unsafe read/write.
 */
var BukkitStack.unsafeNbt: CompoundShadowTag?
    get() = this.handle?.unsafeNbt
    set(value) {
        this.handle?.unsafeNbt = value
    }

/**
 * Unsafe read.
 */
val BukkitStack.unsafeNbtOrThrow: CompoundShadowTag
    get() = this.unsafeNbt ?: throw NoSuchElementException("No NBT present on ItemStack")

/**
 * Safe read/write.
 */
var BukkitStack.nyaTag: CompoundShadowTag?
    get() = this.handle?.nyaTag
    set(value) {
        val handle: MojangStack? = this.handle
        if (handle == null) {
            throw IllegalStateException("Can't write nya tag into empty ItemStack")
        }
        handle.nyaTag = value
    }

/**
 * Safe read.
 */
val BukkitStack.nyaTagOrThrow: CompoundShadowTag
    get() = this.nyaTag ?: throw NoSuchElementException("No nya tag present on ItemStack")

/**
 * Safe edit.
 */
fun BukkitStack.editNyaTag(block: (CompoundShadowTag) -> Unit) {
    this.nyaTag = (this.nyaTag ?: CompoundShadowTag.create()).apply(block)
}

/**
 * Unsafe read/write.
 */
var BukkitStack.unsafeNyaTag: CompoundShadowTag?
    get() {
        val handle: MojangStack? = this.handle
        return handle?.unsafeNyaTag
    }
    set(value) {
        val handle: MojangStack? = this.handle
        if (handle == null) {
            throw IllegalStateException("Can't write nya tag into empty ItemStack")
        }
        handle.unsafeNyaTag = value
    }

/**
 * Unsafe read.
 */
val BukkitStack.unsafeNyaTagOrThrow: CompoundShadowTag
    get() = this.unsafeNyaTag ?: throw NoSuchElementException("No nya tag present on ItemStack")
//</editor-fold>


//<editor-fold desc="MojangStack">
/**
 * 检查物品堆叠是否应该被网络渲染接管.
 *
 * `true` 表示物品堆叠应该被网络渲染接管.
 * `false` 表示物品堆叠不应该被网络渲染接管.
 */
var MojangStack.isClientSide: Boolean
    get() = this.unsafeNbt?.contains(CLIENT_SIDE_KEY) == true
    set(value) {
        if (value) {
            this.editNbt { it.putByte(CLIENT_SIDE_KEY, 0) }
        } else {
            this.editNbt { it.remove(CLIENT_SIDE_KEY) }
        }
    }

/**
 * Safe read/write.
 *
 * ## Read
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 副本,
 * 然后返回副本上的根 NBT.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 副本,
 * 然后将根 NBT 写入副本,
 * 最后将副本写入物品.
 */
private var MojangStack.nbt: CompoundShadowTag?
    get() {
        return this.getCustomData()?.wrap
    }
    set(value) {
        if (value != null) {
            this.setCustomData(value.unwrap)
        } else {
            this.unsetCustomData()
        }
    }

/**
 * Safe edit.
 */
private fun MojangStack.editNbt(block: (CompoundShadowTag) -> Unit) {
    this.nbt = (this.nbt ?: CompoundShadowTag.create()).apply(block)
}

/**
 * Unsafe read/write.
 *
 * ## Read
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后返回引用上的根 NBT.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后将根 NBT 写入引用,
 * 最后将引用写入物品.
 */
private var MojangStack.unsafeNbt: CompoundShadowTag?
    get() {
        return this.getUnsafeCustomData()?.wrap
    }
    set(value) {
        if (value != null) {
            this.setUnsafeCustomData(value.unwrap)
        } else {
            this.unsetCustomData()
        }
    }

/**
 * Safe read/write.
 *
 * ## Read
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 副本,
 * 然后返回副本上的萌芽 NBT.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 副本,
 * 然后将萌芽 NBT 写入副本,
 * 最后将副本写入物品.
 */
private var MojangStack.nyaTag: CompoundShadowTag?
    get() {
        val data = this.getCustomData() ?: return null
        val nyaTag = data.getCompoundOrNull(SharedConstants.ROOT_NBT_NAME) ?: return null
        return nyaTag.wrap
    }
    set(value) {
        val data = this.getCustomData()
        if (data === null) {
            if (value != null) {
                val tag = CompoundTag()
                tag.put(SharedConstants.ROOT_NBT_NAME, value.unwrap)
                this.setCustomData(tag)
            }
        } else {
            if (value != null) {
                data.put(SharedConstants.ROOT_NBT_NAME, value.unwrap)
            } else {
                data.remove(SharedConstants.ROOT_NBT_NAME)
            }
            this.setCustomData(data)
        }
    }

/**
 * Unsafe read/write.
 *
 * ## Read
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后返回引用上的萌芽 NBT.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后将萌芽 NBT 写入引用,
 * 最后将引用写入物品.
 */
private var MojangStack.unsafeNyaTag: CompoundShadowTag?
    get() {
        val data = this.getUnsafeCustomData()
        val nyaTag = data?.getCompoundOrNull(SharedConstants.ROOT_NBT_NAME) ?: return null
        return nyaTag.wrap
    }
    set(value) {
        val data = this.getUnsafeCustomData()
        if (data === null) {
            if (value != null) {
                val tag = CompoundTag()
                tag.put(SharedConstants.ROOT_NBT_NAME, value.unwrap)
                this.setUnsafeCustomData(tag)
            }
        } else {
            if (value != null) {
                data.put(SharedConstants.ROOT_NBT_NAME, value.unwrap)
            } else {
                data.remove(SharedConstants.ROOT_NBT_NAME)
            }
        }
    }

//</editor-fold>

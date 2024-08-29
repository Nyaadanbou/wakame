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

const val NYA_TAG_KEY: String = "wakame"

//<editor-fold desc="Shadow bridge">
internal val Tag.wrap: ShadowTag
    get() = BukkitShadowFactory.global().shadow<ShadowTag>(this)
internal val ShadowTag.unwrap: Tag
    get() = this.shadowTarget as Tag
internal val CompoundTag.wrap: CompoundShadowTag
    get() = BukkitShadowFactory.global().shadow<CompoundShadowTag>(this)
internal val CompoundShadowTag.unwrap: CompoundTag
    get() = this.shadowTarget as CompoundTag
//</editor-fold>

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
        val shadow = BukkitShadowFactory.global().shadow<ShadowItemStack>(this)
        val delegate = shadow.craftDelegate
        delegate.handle
    }?.takeUnless(MojangStack::isEmpty)

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.backingCustomName: Component?
    get() = this.handle?.get(DataComponents.CUSTOM_NAME)?.let(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.CUSTOM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle?.remove(DataComponents.CUSTOM_NAME)
        }
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.backingItemName: Component?
    get() = this.handle?.get(DataComponents.ITEM_NAME)?.let(PaperAdventure::asAdventure)
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.ITEM_NAME, PaperAdventure.asVanilla(value))
        } else {
            this.handle?.remove(DataComponents.ITEM_NAME)
        }
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.backingLore: List<Component>?
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
var BukkitStack.backingCustomModelData: Int?
    get() = this.handle?.get(DataComponents.CUSTOM_MODEL_DATA)?.value
    set(value) {
        if (value != null) {
            this.handle?.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(value))
        } else {
            this.handle?.remove(DataComponents.CUSTOM_MODEL_DATA)
        }
    }

/**
 * Safe read/write.
 */
var BukkitStack.nyaTag: CompoundShadowTag?
    get() {
        val handle: MojangStack? = this.handle
        return handle?.nyaTag
    }
    set(value) {
        val handle: MojangStack? = this.handle
        if (handle == null) {
            throw IllegalStateException("Can't write NBT into empty ItemStack")
        }
        handle.nyaTag = value
    }

/**
 * Safe read.
 */
val BukkitStack.nyaTagOrThrow: CompoundShadowTag
    get() = this.nyaTag ?: throw NoSuchElementException("No nyaa tag present on ItemStack")

/**
 * Safe edit.
 */
fun BukkitStack.editNyaTag(block: (CompoundShadowTag) -> Unit) {
    val nyaTag = this.nyaTag ?: CompoundShadowTag.create()
    nyaTag.apply(block)
    this.nyaTag = nyaTag
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
            throw IllegalStateException("Can't write NBT into empty ItemStack")
        }
        handle.unsafeNyaTag = value
    }

/**
 * Unsafe read.
 */
val BukkitStack.unsafeNyaTagOrThrow: CompoundShadowTag
    get() = this.unsafeNyaTag ?: throw NoSuchElementException("No nya NBT present on ItemStack")

/**
 * Unsafe edit.
 */
fun BukkitStack.unsafeEditNyaTag(block: (CompoundShadowTag) -> Unit) {
    val nyaTag = this.unsafeNyaTag ?: CompoundShadowTag.create()
    nyaTag.apply(block)
    this.unsafeNyaTag = nyaTag
}
//</editor-fold>

//<editor-fold desc="MojangStack">
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
        val data = this.getCustomData() /* copy */ ?: return null
        val nyaTag = data.getCompoundOrNull(NYA_TAG_KEY) ?: return null
        return nyaTag.wrap
    }
    set(value) {
        val data = this.getCustomData() /* copy */
        if (data == null) {
            if (value != null) {
                val tag = CompoundTag()
                tag.put(NYA_TAG_KEY, value.unwrap)
                this.setCustomData(tag)
            }
        } else {
            if (value == null) {
                data.remove(NYA_TAG_KEY)
            } else {
                data.put(NYA_TAG_KEY, value.unwrap)
                this.setCustomData(data)
            }
            if (data.isEmpty) {
                this.unsetCustomData()
            }
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
        val nyaTag = data?.getCompoundOrNull(NYA_TAG_KEY) ?: return null
        return nyaTag.wrap
    }
    set(value) {
        val data = this.getUnsafeCustomData()
        if (data == null) {
            if (value != null) {
                val tag = CompoundTag()
                tag.put(NYA_TAG_KEY, value.unwrap)
                this.setUnsafeCustomData(tag)
            }
        } else {
            if (value == null) {
                data.remove(NYA_TAG_KEY)
            } else {
                data.put(NYA_TAG_KEY, value.unwrap)
            }
            if (data.isEmpty) {
                this.unsetCustomData()
            }
        }
    }

//</editor-fold>

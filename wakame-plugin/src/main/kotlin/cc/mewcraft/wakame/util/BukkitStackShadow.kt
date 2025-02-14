@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.util

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.craftbukkit.inventory.CraftItemStack
import xyz.xenondevs.commons.collections.isNotNullOrEmpty
import cc.mewcraft.nbt.CompoundTag as CompoundShadowTag
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

private const val ROOT_NBT_FIELD = "wakame"
private const val CLIENT_SIDE_KEY = "client_side"

internal val CompoundTag.wrap: CompoundShadowTag
    get() = BukkitShadowFactory.global().shadow<CompoundShadowTag>(this)
internal val CompoundShadowTag.unwrap: CompoundTag
    get() = this.shadowTarget as CompoundTag

//<editor-fold desc="BukkitStack">
/**
 * 将 [BukkitStack] 封装为 [MojangStack].
 */
internal val MojangStack.wrap: BukkitStack
    get() = CraftItemStack.asCraftMirror(this)

/**
 * 获取封装的 NMS 对象.
 * 如果 [isEmpty] 为 `true`, 将会返回 `null`.
 */
internal val BukkitStack.unwrap: MojangStack?
    get() = CraftItemStack.unwrap(this)?.takeUnless(MojangStack::isEmpty)

/**
 * 设置物品是否应该被网络渲染接管.
 */
var BukkitStack.isClientSide: Boolean
    get() = this.unwrap?.isClientSide == true
    set(value) {
        this.unwrap?.isClientSide = value
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.customName: Component?
    get() = this.getData(DataComponentTypes.CUSTOM_NAME)
    set(value) {
        if (value != null) {
            this.setData(DataComponentTypes.CUSTOM_NAME, value)
        } else {
            this.resetData(DataComponentTypes.CUSTOM_NAME)
        }
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.itemName: Component?
    get() = this.getData(DataComponentTypes.ITEM_NAME)
    set(value) {
        if (value != null) {
            this.setData(DataComponentTypes.ITEM_NAME, value)
        } else {
            this.resetData(DataComponentTypes.ITEM_NAME)
        }
    }

/**
 * 设置物品的模型. 你可以传入 `null` 来移除它.
 */
var BukkitStack.itemModel: Key?
    get() = this.getData(DataComponentTypes.ITEM_MODEL)
    set(value) {
        if (value != null) {
            this.setData(DataComponentTypes.ITEM_MODEL, value)
        } else {
            this.resetData(DataComponentTypes.ITEM_MODEL)
        }
    }

/**
 * 设置物品的描述. 你可以传入 `null` 来移除它.
 */
var BukkitStack.itemLore: List<Component>?
    get() = this.getData(DataComponentTypes.LORE)?.lines()
    set(value) {
        if (value.isNotNullOrEmpty()) {
            this.setData(DataComponentTypes.LORE, ItemLore.lore(value))
        } else {
            this.resetData(DataComponentTypes.LORE)
        }
    }

/**
 * 设置自定义模型数据. 你可以传入 `null` 来移除它.
 */
var BukkitStack.customModelData: Int?
    get() = this.getData(DataComponentTypes.CUSTOM_MODEL_DATA)?.id()
    set(value) {
        if (value != null) {
            this.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData(value))
        } else {
            this.resetData(DataComponentTypes.CUSTOM_MODEL_DATA)
        }
    }

/**
 * 设置是否隐藏附加提示.
 */
var BukkitStack.hideAdditionalTooltip: Boolean
    get() = this.hasData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)
    set(value) {
        if (value) {
            this.setData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)
        } else {
            this.resetData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)
        }
    }

/**
 * 设置是否隐藏提示.
 */
var BukkitStack.hideTooltip: Boolean
    get() = this.hasData(DataComponentTypes.HIDE_TOOLTIP)
    set(value) {
        if (value) {
            this.setData(DataComponentTypes.HIDE_TOOLTIP)
        } else {
            this.resetData(DataComponentTypes.HIDE_TOOLTIP)
        }
    }

/**
 * Safe read/write.
 */
var BukkitStack.rootTagOrNull: CompoundShadowTag?
    get() = this.unwrap?.rootTagOrNull
    set(value) {
        this.unwrap?.rootTagOrNull = value
    }

/**
 * Safe read.
 */
val BukkitStack.rootTag: CompoundShadowTag
    get() = this.rootTagOrNull ?: throw NoSuchElementException("No tag present on ItemStack")

/**
 * Safe edit.
 */
fun BukkitStack.editRootTag(block: (CompoundShadowTag) -> Unit) {
    this.rootTagOrNull = (this.rootTagOrNull ?: CompoundShadowTag.create()).apply(block)
}

/**
 * Unsafe read/write.
 */
var BukkitStack.unsafeRootTagOrNull: CompoundShadowTag?
    get() = this.unwrap?.unsafeRootTagOrNull
    set(value) {
        this.unwrap?.unsafeRootTagOrNull = value
    }

/**
 * Unsafe read.
 */
val BukkitStack.unsafeRootTag: CompoundShadowTag
    get() = this.unsafeRootTagOrNull ?: throw NoSuchElementException("No tag present on ItemStack")

/**
 * Safe read/write.
 */
var BukkitStack.nekooTagOrNull: CompoundShadowTag?
    get() = this.unwrap?.nekooTagOrNull
    set(value) {
        val handle: MojangStack? = this.unwrap
        if (handle == null) {
            throw IllegalStateException("Can't write data into empty ItemStack")
        }
        handle.nekooTagOrNull = value
    }

/**
 * Safe read.
 */
val BukkitStack.nekooTag: CompoundShadowTag
    get() = this.nekooTagOrNull ?: throw NoSuchElementException("No tag present on ItemStack")

/**
 * Safe edit.
 */
fun BukkitStack.editNekooTag(block: (CompoundShadowTag) -> Unit) {
    this.nekooTagOrNull = (this.nekooTagOrNull ?: CompoundShadowTag.create()).apply(block)
}

/**
 * Unsafe read/write.
 */
var BukkitStack.unsafeNekooTagOrNull: CompoundShadowTag?
    get() = this.unwrap?.unsafeNekooTagOrNull
    set(value) {
        val handle: MojangStack? = this.unwrap
        if (handle == null) {
            throw IllegalStateException("Can't write nya tag into empty ItemStack")
        }
        handle.unsafeNekooTagOrNull = value
    }

/**
 * Unsafe read.
 */
val BukkitStack.unsafeNekooTag: CompoundShadowTag
    get() = this.unsafeNekooTagOrNull ?: throw NoSuchElementException("No nya tag present on ItemStack")
//</editor-fold>


//<editor-fold desc="MojangStack">
/**
 * 检查物品堆叠是否应该被网络渲染接管.
 *
 * `true` 表示物品堆叠应该被网络渲染接管.
 * `false` 表示物品堆叠不应该被网络渲染接管.
 */
var MojangStack.isClientSide: Boolean
    get() = this.unsafeRootTagOrNull?.contains(CLIENT_SIDE_KEY) == true
    set(value) {
        if (value) {
            this.editRootTag { it.remove(CLIENT_SIDE_KEY) }
        } else {
            this.editRootTag { it.putByte(CLIENT_SIDE_KEY, 0) }
        }
    }

/**
 * Safe read/write.
 *
 * ## Read
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 副本,
 * 然后返回副本上的根标签.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 副本,
 * 然后将根标签写入副本,
 * 最后将副本写入物品.
 */
private var MojangStack.rootTagOrNull: CompoundShadowTag?
    get() = this.getCustomData()?.wrap
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
private fun MojangStack.editRootTag(block: (CompoundShadowTag) -> Unit) {
    this.rootTagOrNull = (this.rootTagOrNull ?: CompoundShadowTag.create()).apply(block)
}

/**
 * Unsafe read/write.
 *
 * ## Read
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后返回引用上的根标签.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后将根标签写入引用,
 * 最后将引用写入物品.
 */
private var MojangStack.unsafeRootTagOrNull: CompoundShadowTag?
    get() = this.getUnsafeCustomData()?.wrap
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
 * 然后返回副本上的萌芽标签.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 副本,
 * 然后将萌芽标签写入副本,
 * 最后将副本写入物品.
 */
private var MojangStack.nekooTagOrNull: CompoundShadowTag?
    get() = this.getCustomData()?.getCompoundOrNull(ROOT_NBT_FIELD)?.wrap
    set(value) {
        val data = this.getCustomData()
        if (data === null) {
            if (value != null) {
                val tag = CompoundTag()
                tag.put(ROOT_NBT_FIELD, value.unwrap)
                this.setCustomData(tag)
            }
        } else {
            if (value != null) {
                data.put(ROOT_NBT_FIELD, value.unwrap)
            } else {
                data.remove(ROOT_NBT_FIELD)
            }
            this.setCustomData(data)
        }
    }

/**
 * Unsafe read/write.
 *
 * ## Read
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后返回引用上的萌芽标签.
 *
 * ## Write
 * 读取 `minecraft:custom_data` 的 [CompoundTag] 引用,
 * 然后将萌芽标签写入引用,
 * 最后将引用写入物品.
 */
private var MojangStack.unsafeNekooTagOrNull: CompoundShadowTag?
    get() = this.getUnsafeCustomData()?.getCompoundOrNull(ROOT_NBT_FIELD)?.wrap
    set(value) {
        val data = this.getUnsafeCustomData()
        if (data === null) {
            if (value != null) {
                val tag = CompoundTag()
                tag.put(ROOT_NBT_FIELD, value.unwrap)
                this.setUnsafeCustomData(tag)
            }
        } else {
            if (value != null) {
                data.put(ROOT_NBT_FIELD, value.unwrap)
            } else {
                data.remove(ROOT_NBT_FIELD)
            }
        }
    }

//</editor-fold>

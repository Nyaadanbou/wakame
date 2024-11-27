package cc.mewcraft.wakame.util

import cc.mewcraft.nbt.CompoundTag
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.enchantment.ItemEnchantments
import org.bukkit.inventory.ItemStack
import net.minecraft.world.item.ItemStack as MojangStack

/**
 * 直接修改一个 [ItemStack]. 暂时仅支持部分常用数据.
 */
fun ItemStack.edit(block: ItemStackDSL.() -> Unit): ItemStack {
    return ItemStackDSL(this).apply(block).target
}

class ItemStackDSL(
    internal val target: ItemStack,
) {
    var itemName
        get() = target.itemName
        set(value) {
            target.itemName = value
        }

    var customName
        get() = target.customName
        set(value) {
            target.customName = value
        }

    var lore
        get() = target.lore0
        set(value) {
            target.lore0 = value
        }

    var customModelData
        get() = target.customModelData
        set(value) {
            target.customModelData = value
        }

    var hideAdditionalTooltip: Boolean
        get() = target.hideAdditionalTooltip
        set(value) {
            target.hideAdditionalTooltip = value
        }

    var hideTooltip: Boolean
        get() = target.hideTooltip
        set(value) {
            target.hideTooltip = value
        }

    var customData: CompoundTag?
        get() = target.rootTagOrNull
        set(value) {
            target.rootTagOrNull = value
        }

    //<editor-fold desc="`show_in_tooltip`">
    fun showAttributeModifiers(value: Boolean) {
        target.showAttributeModifiers(value)
    }

    fun showCanBreak(value: Boolean) {
        target.showCanBreak(value)
    }

    fun showCanPlaceOn(value: Boolean) {
        target.showCanPlaceOn(value)
    }

    fun showDyedColor(value: Boolean) {
        target.showDyedColor(value)
    }

    fun showEnchantments(value: Boolean) {
        target.showEnchantments(value)
    }

    fun showJukeboxPlayable(value: Boolean) {
        target.showJukeboxPlayable(value)
    }

    fun showStoredEnchantments(value: Boolean) {
        target.showStoredEnchantments(value)
    }

    fun showTrim(value: Boolean) {
        target.showTrim(value)
    }

    fun showUnbreakable(value: Boolean) {
        target.showUnbreakable(value)
    }

    fun showNothing() {
        target.showNothing()
    }
    //</editor-fold>
}

/**
 * 检查物品是否允许被损耗.
 */
val ItemStack.isDamageable: Boolean
    get() = handle?.isDamageableItem == true

/**
 * 设置物品的损耗, 这将使物品变为可损耗的物品, 也就是 [isDamageable] 返回 `true`.
 */
fun ItemStack.setDamageable(maxDamage: Int, damage: Int = 0) {
    val handle = handle ?: return
    require(maxDamage > 0) { "Max damage must be positive." }
    require(damage >= 0) { "Damage must be non-negative." }
    handle.remove(DataComponents.UNBREAKABLE)
    handle.set(DataComponents.MAX_DAMAGE, maxDamage)
    handle.set(DataComponents.DAMAGE, damage)
}

/**
 * 取消物品的损耗, 这将使物品变为不可损耗的物品, 也就是 [isDamageable] 返回 `false`.
 */
fun ItemStack.unsetDamageable() {
    val handle = handle ?: return
    handle.remove(DataComponents.MAX_DAMAGE)
    handle.remove(DataComponents.DAMAGE)
}

/**
 * 检查物品是否损耗.
 */
val ItemStack.isDamaged: Boolean
    get() = handle?.isDamaged == true

/**
 * 获取物品当前的损耗.
 * 必须先检查 [isDamageable] 是否为 `true`.
 */
var ItemStack.damage: Int
    get() = handle?.damageValue ?: 0
    set(value) = (handle?.damageValue = value)

/**
 * 获取物品的最大损耗.
 * 必须先检查 [isDamageable] 是否为 `true`.
 */
var ItemStack.maxDamage: Int
    get() = handle?.maxDamage ?: 0
    set(value) {
        val handle = handle ?: return
        handle.set(DataComponents.MAX_DAMAGE, value.coerceAtLeast(1))
        handle.set(DataComponents.DAMAGE, damage.coerceIn(0, value))
    }

//<editor-fold desc="`show_in_tooltip`">
private val EMPTY_ATTRIBUTE_MODIFIERS = ItemAttributeModifiers.EMPTY.withTooltip(false)

// FIXME 等 Mojang 修复: https://bugs.mojang.com/browse/MC-271826.
//  目前我们无法通过正常的途径 `!attribute_modifiers` 移除盔甲上的默认属性.
//  暂时一刀切, 无论什么情况都改为空属性修饰符+不显示, 等 Mojang 修复后再优化.
fun ItemStack.showAttributeModifiers(value: Boolean) {
    val handle = handle ?: return

    /* val data = handle.get(DataComponents.ATTRIBUTE_MODIFIERS) ?: return
    // 对于所有盔甲物品, 即使它们有属性修饰符的加成, 但这里获取到的依然是 `EMPTY` (MC-271826).
    // 也就是说, 我们无法准确的通过 ItemAttributeModifiers 来判断一个盔甲物品是否有属性修饰符.
    if ((data === ItemAttributeModifiers.EMPTY || data.modifiers.isEmpty()) && handle.item !is ArmorItem) {
        return
    } */

    if (value) {
        handle.modify(DataComponents.ATTRIBUTE_MODIFIERS) { data -> data.withTooltip(true) }
    } else {
        handle.set(DataComponents.ATTRIBUTE_MODIFIERS, EMPTY_ATTRIBUTE_MODIFIERS)
    }
}

fun ItemStack.showCanBreak(value: Boolean) {
    handle?.modify(DataComponents.CAN_BREAK) { data -> data.withTooltip(value) }
}

fun ItemStack.showCanPlaceOn(value: Boolean) {
    handle?.modify(DataComponents.CAN_PLACE_ON) { data -> data.withTooltip(value) }
}

fun ItemStack.showDyedColor(value: Boolean) {
    handle?.modify(DataComponents.DYED_COLOR) { data -> data.withTooltip(value) }
}

fun ItemStack.showEnchantments(value: Boolean) {
    val handle = handle ?: return
    val data = handle.get(DataComponents.ENCHANTMENTS) ?: return
    if (data === ItemEnchantments.EMPTY || data.isEmpty) {
        return
    }
    handle.modify(DataComponents.ENCHANTMENTS) { data1 -> data1.withTooltip(value) }
}

fun ItemStack.showJukeboxPlayable(value: Boolean) {
    handle?.modify(DataComponents.JUKEBOX_PLAYABLE) { data -> data.withTooltip(value) }
}

fun ItemStack.showStoredEnchantments(value: Boolean) {
    handle?.modify(DataComponents.STORED_ENCHANTMENTS) { data -> data.withTooltip(value) }
}

fun ItemStack.showTrim(value: Boolean) {
    handle?.modify(DataComponents.TRIM) { data -> data.withTooltip(value) }
}

fun ItemStack.showUnbreakable(value: Boolean) {
    handle?.modify(DataComponents.UNBREAKABLE) { data -> data.withTooltip(value) }
}

fun ItemStack.showNothing() {
    showAttributeModifiers(false)
    showCanBreak(false)
    showCanPlaceOn(false)
    showDyedColor(false)
    showEnchantments(false)
    showJukeboxPlayable(false)
    showStoredEnchantments(false)
    showTrim(false)
    showUnbreakable(false)
}
//</editor-fold>

private fun <T> MojangStack.modify(type: DataComponentType<T>, block: (T) -> T) {
    val oldData = get(type) ?: return
    val newData = block(oldData)
    set(type, newData)
}
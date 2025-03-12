@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.util.item

import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toAdventureComponent
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
fun ItemStack?.isEmpty(): Boolean {
    contract {
        returns(false) implies (this@isEmpty != null)
    }
    return this == null || isEmpty
}

fun ItemStack.takeUnlessEmpty(): ItemStack? =
    if (isEmpty) null else this

/**
 * 将 [ItemStack] 转换成一个 JSON 字符串 (可读性比 [ItemStack.toString] 要高很多).
 */
fun ItemStack?.toJsonString(): String {
    return if (this != null) Bukkit.getUnsafe().serializeItemAsJson(this).toString() else "null"
}

/**
 * 在 [ItemStack] 不为空时执行指定代码块.
 */
fun ItemStack.whenNotEmpty(block: ItemStack.() -> Unit) {
    if (!isEmpty) block(this)
}

/**
 * 在 [ItemStack] 不为空时执行指定代码块, 并返回其结果.
 */
fun <T> ItemStack.whenNotEmptyReturn(block: ItemStack.() -> T): T? {
    return if (!isEmpty) block(this) else null
}

// ItemStack 的某些定义取决于多个物品组件的状态, 例如判断一个 ItemStack 是否为 damageable
// 这里特别把这些特殊的定义写成扩展函数, 方便我们直接调用
// 不然的话就得手动, 把完整的判断逻辑写一遍又一遍

val ItemStack.isDamageable: Boolean get() = unwrapToMojang().isDamageableItem
val ItemStack.isDamaged: Boolean get() = unwrapToMojang().isDamaged
var ItemStack.damage: Int
    get() = unwrapToMojang().damageValue
    set(value) = whenNotEmpty { unwrapToMojang().damageValue = value }
val ItemStack.maxDamage: Int get() = unwrapToMojang().maxDamage
val ItemStack.shouldBreak: Boolean get() = unwrapToMojang().isBroken
val ItemStack.willBreakNextUse: Boolean get() = unwrapToMojang().nextDamageWillBreak()


// 这些操作在项目中被高频使用, 创建这些扩展函数以减少重复代码
// 特意只声明了 val 以消除扩展函数所带来的任何歧义 (例如设置为 null 到底是 unset/reset)

val ItemStack.itemName: Component? get() = getData(DataComponentTypes.ITEM_NAME)
val ItemStack.itemNameOrType: Component get() = getData(DataComponentTypes.ITEM_NAME) ?: Component.translatable(type)
val ItemStack.customName: Component? get() = getData(DataComponentTypes.CUSTOM_NAME)
val ItemStack.fastLore: List<Component>? get() = unwrapToMojang().lore
val ItemStack.fastLoreOrEmpty: List<Component> get() = unwrapToMojang().loreOrEmpty
fun ItemStack.fastLore(lore: List<Component>) = unwrapToMojang().lore(lore)
fun ItemStack.toHoverableComponent(): Component = unwrapToMojang().hoverName.toAdventureComponent()

val ItemStack.nbtCopy: CompoundTag? get() = unwrapToMojang().nbtCopy

@Deprecated("Do not make any changes to the returned NBT element", ReplaceWith("nbt"))
val ItemStack.nbt: CompoundTag? get() = unwrapToMojang().nbt

fun ItemStack.setNBT(nbt: CompoundTag) = unwrapToMojang().setNbt(nbt)
fun ItemStack.editNbt(create: Boolean = true, applier: (CompoundTag) -> Unit) = unwrapToMojang().editNbt(create, applier)
fun ItemStack.removeNBT() = unwrapToMojang().removeNbt()


// 网络数据包相关

var ItemStack.isNetworkRewrite: Boolean
    get() = unwrapToMojang().isNetworkRewrite
    set(value) = whenNotEmpty { unwrapToMojang().isNetworkRewrite = value }


// 用于将数据隐藏于 ItemStack 的提示框
// 1.20.5/1.21.5 这两版本对于该机制的实现有非常大的变化
// 设置这些扩展函数, 是为了降低 Koish 需要变动的代码, 以及提高代码的性能

private fun <T : Any> ItemStack.hideData(type: DataComponentType<T>, change: (T) -> T) = whenNotEmpty {
    val item = unwrapToMojang()
    val oldData = item.get(type) ?: return@whenNotEmpty // 如果没有数据就不需要隐藏, 可以直接返回
    val newData = change(oldData)
    item.set(type, newData)
}

fun ItemStack.hideAll() = whenNotEmpty {
    hideDyedColor()
    hideCanBreak()
    hideCanPlaceOn()
    hideTrim()
    hideAttributeModifiers()
    hideEnchantments()
    hideStoredEnchantments()
    hideJukeboxPlayable()
    hideUnbreakable()
}

fun ItemStack.hideDyedColor() = hideData(DataComponents.DYED_COLOR) { it.withTooltip(false) }
fun ItemStack.hideCanBreak() = hideData(DataComponents.CAN_BREAK) { it.withTooltip(false) }
fun ItemStack.hideCanPlaceOn() = hideData(DataComponents.CAN_PLACE_ON) { it.withTooltip(false) }
fun ItemStack.hideTrim() = hideData(DataComponents.TRIM) { it.withTooltip(false) }
fun ItemStack.hideAttributeModifiers() = hideData(DataComponents.ATTRIBUTE_MODIFIERS) { it.withTooltip(false) }
fun ItemStack.hideEnchantments() = hideData(DataComponents.ENCHANTMENTS) { it.withTooltip(false) }
fun ItemStack.hideStoredEnchantments() = hideData(DataComponents.STORED_ENCHANTMENTS) { it.withTooltip(false) }
fun ItemStack.hideJukeboxPlayable() = hideData(DataComponents.JUKEBOX_PLAYABLE) { it.withTooltip(false) }
fun ItemStack.hideUnbreakable() = hideData(DataComponents.UNBREAKABLE) { it.withTooltip(false) }


// Bukkit & Mojang 互相转换

fun MojangStack?.wrapToBukkit(): ItemStack = CraftItemStack.asCraftMirror(this)
fun ItemStack.unwrapToMojang(): MojangStack = CraftItemStack.unwrap(this)


// Mojang ItemStack 的扩展函数

inline fun MojangStack.whenNotEmpty(block: MojangStack.() -> Unit) {
    if (!isEmpty) block(this)
}

inline fun <T> MojangStack.whenNotEmptyReturn(block: MojangStack.() -> T): T? {
    return if (!isEmpty) block(this) else null
}

val MojangStack.lore: List<Component>? get() = get(DataComponents.LORE)?.lines?.map(net.minecraft.network.chat.Component::toAdventureComponent)
val MojangStack.loreOrEmpty: List<Component> get() = lore ?: emptyList()
fun MojangStack.lore(lore: List<Component>) = whenNotEmpty { set(DataComponents.LORE, ItemLore(lore.map(Component::toNMSComponent))) }

val MojangStack.nbtCopy: CompoundTag? get() = get(DataComponents.CUSTOM_DATA)?.copyTag()

@Deprecated("Do not make any changes to the returned NBT element", ReplaceWith("nbt"))
val MojangStack.nbt: CompoundTag? get() = get(DataComponents.CUSTOM_DATA)?.unsafe
fun MojangStack.setNbt(nbt: CompoundTag) = whenNotEmpty { set(DataComponents.CUSTOM_DATA, CustomData.of(nbt)) }
fun MojangStack.editNbt(create: Boolean = true, applier: (CompoundTag) -> Unit) = whenNotEmpty {
    val nbt = nbtCopy ?: if (create) CompoundTag() else return
    applier(nbt)
    if (nbt.isEmpty) {
        remove(DataComponents.CUSTOM_DATA)
        return
    }
    setNbt(nbt)
}

fun MojangStack.removeNbt() =
    whenNotEmpty { remove(DataComponents.CUSTOM_DATA) }

fun <T : Any> MojangStack.fastUpdate(type: DataComponentType<T>, default: () -> T, applier: (T) -> T): T? =
    whenNotEmptyReturn { set(type, applier(get(type) ?: default())) } // 快在 lambda default() 只有数据不存在时才会运行

fun <T : Any, U : Any> MojangStack.fastUpdate(type: DataComponentType<T>, default: () -> T, change: U, applier: (T, U) -> T): T? =
    whenNotEmptyReturn { set(type, applier(get(type) ?: default(), change)) }

private const val BYPASS_NETWORK_REWRITE_FIELD = "bypass_network_rewrite"

/**
 * 检查物品堆叠是否应该在网络发包时重写.
 */
var MojangStack.isNetworkRewrite: Boolean
    // 大部分情况都需要重写, 因此实现上用“不包含”来表示“要重写”
    get() {
        val nbt = nbt
        if (nbt == null) return true
        return !nbt.contains(BYPASS_NETWORK_REWRITE_FIELD)
    }
    set(value) = editNbt { nbt ->
        if (value) nbt.remove(BYPASS_NETWORK_REWRITE_FIELD)
        else nbt.put(BYPASS_NETWORK_REWRITE_FIELD, ByteTag.ZERO)
    }

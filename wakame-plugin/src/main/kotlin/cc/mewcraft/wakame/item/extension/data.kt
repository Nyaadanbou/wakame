package cc.mewcraft.wakame.item.extension

import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.item.customName
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.fastLore
import cc.mewcraft.wakame.util.item.fastLoreOrEmpty
import cc.mewcraft.wakame.util.item.hideAll
import cc.mewcraft.wakame.util.item.hideAttributeModifiers
import cc.mewcraft.wakame.util.item.hideCanBreak
import cc.mewcraft.wakame.util.item.hideCanPlaceOn
import cc.mewcraft.wakame.util.item.hideDyedColor
import cc.mewcraft.wakame.util.item.hideEnchantments
import cc.mewcraft.wakame.util.item.hideJukeboxPlayable
import cc.mewcraft.wakame.util.item.hideStoredEnchantments
import cc.mewcraft.wakame.util.item.hideTrim
import cc.mewcraft.wakame.util.item.hideUnbreakable
import cc.mewcraft.wakame.util.item.isDamageable
import cc.mewcraft.wakame.util.item.isDamaged
import cc.mewcraft.wakame.util.item.itemName
import cc.mewcraft.wakame.util.item.itemNameOrType
import cc.mewcraft.wakame.util.item.maxDamage
import cc.mewcraft.wakame.util.item.removeNBT
import cc.mewcraft.wakame.util.item.setNBT
import cc.mewcraft.wakame.util.item.shouldBreak
import cc.mewcraft.wakame.util.item.toHoverableComponent
import cc.mewcraft.wakame.util.item.willBreakNextUse
import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.inventory.ItemStack
import java.util.Collections.emptySet
import kotlin.reflect.KProperty


// 用于直接操作物品堆叠上的原版数据 (Paper DataComponent API)

fun <T : Any> NekoStack.getMinecraftData(type: DataComponentType.Valued<T>): T? = this.bukkitStack.getData(type)
fun <T : Any> NekoStack.getMinecraftDataOrDefault(type: DataComponentType.Valued<T>, fallback: T): T = this.bukkitStack.getDataOrDefault(type, fallback)!!
fun <T : Any> NekoStack.hasMinecraftData(type: DataComponentType): Boolean = this.bukkitStack.hasData(type)
fun NekoStack.getMinecraftDataTypes(): Set<DataComponentType> = this.bukkitStack.dataTypes
fun <T : Any> NekoStack.setMinecraftData(type: DataComponentType.Valued<T>, valueBuilder: DataComponentBuilder<T>): Unit = this.bukkitStack.setData(type, valueBuilder)
fun <T : Any> NekoStack.setMinecraftData(type: DataComponentType.Valued<T>, value: T): Unit = this.bukkitStack.setData(type, value)
fun NekoStack.setMinecraftData(type: DataComponentType.NonValued): Unit = this.bukkitStack.setData(type)
fun NekoStack.unsetMinecraftData(type: DataComponentType): Unit = this.bukkitStack.unsetData(type)
fun NekoStack.resetMinecraftData(type: DataComponentType): Unit = this.bukkitStack.resetData(type)
fun NekoStack.isMinecraftDataOverridden(type: DataComponentType): Boolean = this.bukkitStack.isDataOverridden(type)
fun NekoStack.matchesWithoutMinecraftData(item: ItemStack, excludeTypes: Set<DataComponentType>): Boolean = this.bukkitStack.matchesWithoutData(item, excludeTypes)
fun NekoStack.matchesWithoutMinecraftData(item: ItemStack, excludeTypes: Set<DataComponentType>, ignoreCount: Boolean): Boolean = this.bukkitStack.matchesWithoutData(item, excludeTypes, ignoreCount)


// 用于方便操作具有特殊定义的物品数据

val NekoStack.isDamageable: Boolean get() = this.bukkitStack.isDamageable
val NekoStack.isDamaged: Boolean get() = this.bukkitStack.isDamaged
var NekoStack.damage: Int
    get() = this.bukkitStack.damage
    set(value) {
        this.bukkitStack.damage = value
    }
val NekoStack.maxDamage: Int get() = this.bukkitStack.maxDamage
val NekoStack.shouldBreak: Boolean get() = this.bukkitStack.shouldBreak
val NekoStack.willBreakNextUse: Boolean get() = this.bukkitStack.willBreakNextUse


// 会被高频使用的操作

val NekoStack.itemName: Component? get() = this.bukkitStack.itemName
val NekoStack.itemNameOrType: Component get() = this.bukkitStack.itemNameOrType
val NekoStack.customName: Component? get() = this.bukkitStack.customName
val NekoStack.fastLore: List<Component>? get() = this.bukkitStack.fastLore
val NekoStack.fastLoreOrEmpty: List<Component> get() = this.bukkitStack.fastLoreOrEmpty
fun NekoStack.fastLore(lore: List<Component>) = this.bukkitStack.fastLore(lore)
fun NekoStack.toHoverableComponent(): Component = this.bukkitStack.toHoverableComponent()
fun NekoStack.setNbt(nbt: CompoundTag) = this.bukkitStack.setNBT(nbt)
fun NekoStack.removeNbt() = this.bukkitStack.removeNBT()


// 用于将特定数据隐藏于提示框

fun NekoStack.hideAll() = this.bukkitStack.hideAll()
fun NekoStack.hideDyedColor() = this.bukkitStack.hideDyedColor()
fun NekoStack.hideCanBreak() = this.bukkitStack.hideCanBreak()
fun NekoStack.hideCanPlaceOn() = this.bukkitStack.hideCanPlaceOn()
fun NekoStack.hideTrim() = this.bukkitStack.hideTrim()
fun NekoStack.hideAttributeModifiers() = this.bukkitStack.hideAttributeModifiers()
fun NekoStack.hideEnchantments() = this.bukkitStack.hideEnchantments()
fun NekoStack.hideStoredEnchantments() = this.bukkitStack.hideStoredEnchantments()
fun NekoStack.hideJukeboxPlayable() = this.bukkitStack.hideJukeboxPlayable()
fun NekoStack.hideUnbreakable() = this.bukkitStack.hideUnbreakable()


// 用于快速访问 Koish 物品数据的扩展函数

var NekoStack.level: Int by mapped(ItemComponentTypes.LEVEL, ItemLevel::minimumLevel, ::ItemLevel, ItemLevel::level)
var NekoStack.rarity: RegistryEntry<Rarity> by mapped(ItemComponentTypes.RARITY, KoishRegistries2.RARITY::getDefaultEntry, ::ItemRarity, ItemRarity::rarity)
var NekoStack.elements: Set<RegistryEntry<ElementType>> by mapped(ItemComponentTypes.ELEMENTS, ::emptySet, ::ItemElements, ItemElements::elements)
var NekoStack.kizamiz: Set<RegistryEntry<KizamiType>> by mapped(ItemComponentTypes.KIZAMIZ, ::emptySet, ::ItemKizamiz, ItemKizamiz::kizamiz)
var NekoStack.reforgeHistory: ReforgeHistory by direct(ItemComponentTypes.REFORGE_HISTORY, ReforgeHistory.ZERO)
var NekoStack.cells: ItemCells? by direct(ItemComponentTypes.CELLS)
var NekoStack.portableCore: PortableCore? by direct(ItemComponentTypes.PORTABLE_CORE)


// 内部实现 (不要细看)

private fun <T> direct(type: ItemComponentType<T>): SimpleComponentDelegate<T> = SimpleComponentDelegate(type)
private fun <T> direct(type: ItemComponentType<T>, default: T): SimpleWithDefaultComponentDelegate<T> = SimpleWithDefaultComponentDelegate(type, default)
private fun <T, R> mapped(type: ItemComponentType<T>, boxFun: (R) -> T, unboxFun: (T) -> R): MappedComponentDelegate<T, R> = MappedComponentDelegate(type, boxFun, unboxFun)
private fun <T, R> mapped(type: ItemComponentType<T>, default: () -> R, boxFun: (R) -> T, unboxFun: (T) -> R): MappedWithDefaultComponentDelegate<T, R> = MappedWithDefaultComponentDelegate(type, default, boxFun, unboxFun)

private class SimpleComponentDelegate<T>(
    private val type: ItemComponentType<T>,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): T? {
        return thisRef.components.get(type)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, value)
        }
    }
}

private class SimpleWithDefaultComponentDelegate<T>(
    private val type: ItemComponentType<T>,
    private val default: T,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): T {
        return thisRef.components.getOrDefault(type, default)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, value)
        }
    }
}

private class MappedComponentDelegate<T, R>(
    private val type: ItemComponentType<T>,
    private val boxFun: (R) -> T,
    private val unboxFun: (T) -> R,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): R? {
        return thisRef.components.get(type)?.let(unboxFun)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: R?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, boxFun(value))
        }
    }
}

private class MappedWithDefaultComponentDelegate<T, R>(
    private val type: ItemComponentType<T>,
    private val default: () -> R,
    private val boxFun: (R) -> T,
    private val unboxFun: (T) -> R,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): R {
        return unboxFun(thisRef.components.getOrDefault(type) { boxFun(default()) })
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: R?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, boxFun(value))
        }
    }
}
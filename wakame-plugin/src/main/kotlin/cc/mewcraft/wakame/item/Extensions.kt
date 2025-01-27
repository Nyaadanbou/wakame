package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.rarity.RarityType
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.damage
import cc.mewcraft.wakame.util.isDamageable
import cc.mewcraft.wakame.util.itemLore
import cc.mewcraft.wakame.util.maxDamage
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import kotlin.reflect.KProperty

val NekoItem.universalId: String
    get() {
        val nekoItemId = this.id
        return "${nekoItemId.namespace()}/${nekoItemId.value()}"
    }

val NekoStack.universalId: String
    get() {
        val nekoItemId = this.id
        return "${nekoItemId.namespace()}/${nekoItemId.value()}"
    }

val NekoItem.modelKey: Key
    get() = Key.key(SharedConstants.PLUGIN_NAME, universalId)

val NekoStack.modelKey: Key
    get() = Key.key(SharedConstants.PLUGIN_NAME, universalId)

/**
 * 获取物品堆叠的数量.
 */
var NekoStack.amount: Int
    get() = wrapped.amount
    set(value) {
        wrapped.amount = value
    }

/**
 * 使物品的攻击进入冷却.
 */
// TODO 让视觉冷却仅应用于萌芽物品类型, 而非 Minecraft 物品类型.
//  升级 1.21.2 后使用 use_cooldown 组件即可解决这个问题.
fun NekoStack.applyAttackCooldown(player: Player) {
    val itemAttackSpeed = this.components.get(ItemComponentTypes.ATTACK_SPEED) ?: return
    val attackSpeedLevel = itemAttackSpeed.level
    val user = player.toUser()
    // 设置实际冷却
    user.attackSpeed.activate(this.id, attackSpeedLevel)
    // 应用视觉冷却 (即末影珍珠使用后的白色覆盖层特效)
    player.setCooldown(this.itemType, attackSpeedLevel.cooldown)
}

/**
 * 获取物品堆叠的损耗值.
 */
var NekoStack.damage: Int
    get() = wrapped.damage
    set(value) {
        wrapped.damage = value
    }

/**
 * 获取物品堆叠的最大损耗值.
 */
val NekoStack.maxDamage: Int
    get() = wrapped.maxDamage

/**
 * 获取物品堆叠是否具有损耗值.
 */
val NekoStack.isDamageable: Boolean
    get() = wrapped.isDamageable

/**
 * 使用该方法使物品失去最后一点耐久时, 不会有损坏动画效果.
 */
fun NekoStack.hurtAndBreak(player: Player, amount: Int) {
    wrapped.damage(amount, player)
}

/**
 * Damages the itemstack in this slot by the specified amount.
 *
 * This runs all logic associated with damaging an itemstack like
 * gamemode and enchantment checks, events, stat changes, advancement triggers,
 * and notifying clients to play break animations.
 *
 * 该函数会特殊处理自定义攻击特效与原版损耗机制之间的交互,
 * 以便修复“重复”损耗物品的问题. 因此, 几乎在所有情况下,
 * 程序员应该使用这个函数来增加物品的损耗.
 *
 * @param slot the slot of the stack to damage
 * @param amount the amount of damage to do
 */
fun Player.damageItemStack2(slot: EquipmentSlot, amount: Int) {
    // 执行 Paper 的逻辑
    damageItemStack(slot, amount)
    // 正确处理此次的损耗
    ItemDamageEventMarker.markAlreadyDamaged(this)
}

var NekoStack.customModelData: Int? by mapped(ItemComponentTypes.CUSTOM_MODEL_DATA, ::CustomModelData, CustomModelData::data)

var NekoStack.customName: Component? by direct(ItemComponentTypes.CUSTOM_NAME)

var NekoStack.itemName: Component? by direct(ItemComponentTypes.ITEM_NAME)

var NekoStack.lore: List<Component>
    get() = wrapped.itemLore.orEmpty()
    set(value) {
        wrapped.itemLore = value
    }

var NekoStack.damageResistant: DamageResistant? by direct(ItemComponentTypes.DAMAGE_RESISTANT)

var NekoStack.level: Int by mapped(ItemComponentTypes.LEVEL, ItemLevel::minimumLevel, ::ItemLevel, ItemLevel::level)

var NekoStack.rarity: RegistryEntry<RarityType> by mapped(ItemComponentTypes.RARITY, KoishRegistries.RARITY::getDefaultEntry, ::ItemRarity, ItemRarity::rarity)

var NekoStack.elements: Set<RegistryEntry<ElementType>> by mapped(ItemComponentTypes.ELEMENTS, ::emptySet, ::ItemElements, ItemElements::elements)

var NekoStack.kizamiz: Set<RegistryEntry<KizamiType>> by mapped(ItemComponentTypes.KIZAMIZ, ::emptySet, ::ItemKizamiz, ItemKizamiz::kizamiz)

var NekoStack.reforgeHistory: ReforgeHistory by direct(ItemComponentTypes.REFORGE_HISTORY, ReforgeHistory.ZERO)

var NekoStack.cells: ItemCells? by direct(ItemComponentTypes.CELLS)

var NekoStack.portableCore: PortableCore? by direct(ItemComponentTypes.PORTABLE_CORE)


//<editor-fold desc="Internal Implementations">
private fun <T> direct(type: ItemComponentType<T>): SimpleComponentDelegate<T> {
    return SimpleComponentDelegate(type)
}

private fun <T> direct(type: ItemComponentType<T>, def: T): SimpleWithDefaultComponentDelegate<T> {
    return SimpleWithDefaultComponentDelegate(type, def)
}

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
    private val def: T,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): T {
        return thisRef.components.getOrDefault(type, def)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, value)
        }
    }
}

private fun <T, R> mapped(type: ItemComponentType<T>, box: (R) -> T, unbox: (T) -> R): MappedComponentDelegate<T, R> {
    return MappedComponentDelegate(type, box, unbox)
}

private fun <T, R> mapped(type: ItemComponentType<T>, def: () -> R, box: (R) -> T, unbox: (T) -> R): MappedWithDefaultComponentDelegate<T, R> {
    return MappedWithDefaultComponentDelegate(type, def, box, unbox)
}

private class MappedComponentDelegate<T, R>(
    private val type: ItemComponentType<T>,
    private val box: (R) -> T,
    private val unbox: (T) -> R,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): R? {
        return thisRef.components.get(type)?.let(unbox)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: R?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, box(value))
        }
    }
}

private class MappedWithDefaultComponentDelegate<T, R>(
    private val type: ItemComponentType<T>,
    private val def: () -> R,
    private val box: (R) -> T,
    private val unbox: (T) -> R,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): R {
        return unbox(thisRef.components.getOrDefault(type) { box(def()) })
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: R?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, box(value))
        }
    }
}
//</editor-fold>

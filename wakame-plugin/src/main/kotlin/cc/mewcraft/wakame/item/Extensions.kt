package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.CustomModelData
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.StandaloneCell
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.MenuIcon
import cc.mewcraft.wakame.util.MenuIconDictionary
import cc.mewcraft.wakame.util.MenuIconLore
import cc.mewcraft.wakame.util.damage
import cc.mewcraft.wakame.util.isDamageable
import cc.mewcraft.wakame.util.itemName
import cc.mewcraft.wakame.util.lore0
import cc.mewcraft.wakame.util.maxDamage
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KProperty

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

//<editor-fold desc="Menu Icon Extensions">
// 移除对于虚拟箱子菜单图标无用的物品组件数据
private fun NekoStack.reduceForMenuIcon(): NekoStack {
    erase()
    unsafeEdit { showNothing() }
    isClientSide = false // 防止客户端显示物品信息
    return this
}

/**
 * 解析物品的:
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconDict]
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconName]
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconLore]
 *
 * 并把这些内容应用到此物品堆叠上.
 */
fun NekoStack.applyMenuIconEverything(dsl: MenuIconLore.LineConfig.Builder.() -> Unit = {}): NekoStack {
    val resolution = prototype.resolveMenuIcon(dsl)
    this.itemName = resolution.name
    this.lore = resolution.lore
    return reduceForMenuIcon()
}

/**
 * 解析物品的:
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconDict]
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconName]
 *
 * 并把这些内容应用到此物品堆叠上.
 */
fun NekoStack.applyMenuIconName(dsl: MenuIcon.PlaceholderTagResolverBuilder.() -> Unit = {}): NekoStack {
    val dict = templates.get(ItemTemplateTypes.MENU_ICON_DICT)?.delegate ?: MenuIconDictionary()
    this.itemName = templates.get(ItemTemplateTypes.MENU_ICON_NAME)?.resolve(MenuIcon.PlaceholderTagResolverBuilder(dict).apply(dsl).build())
    return reduceForMenuIcon()
}

/**
 * 解析物品的:
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconDict]
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconLore]
 *
 * 并把这些内容应用到此物品堆叠上.
 */
fun NekoStack.applyMenuIconLore(dsl: MenuIconLore.LineConfig.Builder.() -> Unit = {}): NekoStack {
    val dict = templates.get(ItemTemplateTypes.MENU_ICON_DICT)?.delegate ?: MenuIconDictionary()
    this.lore = templates.get(ItemTemplateTypes.MENU_ICON_LORE)?.resolve(dict, dsl).orEmpty()
    return reduceForMenuIcon()
}

/**
 * [MenuIcon] 的解析结果.
 */
@ConsistentCopyVisibility
data class MenuIconResolution internal constructor(val name: Component?, val lore: List<Component>) {
    /**
     * 将此 [MenuIconResolution] 应用到 [item].
     *
     * @return
     */
    fun applyNameAndLoreTo(item: ItemStack): ItemStack {
        item.itemName = name
        item.lore0 = lore
        return item
    }
}

/**
 * 解析物品的:
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconDict]
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconName]
 * - [cc.mewcraft.wakame.item.templates.virtual.ItemMenuIconLore]
 *
 * 并把这些内容应用到此物品堆叠上.
 */
fun NekoItem.resolveMenuIcon(dsl: MenuIconLore.LineConfig.Builder.() -> Unit = {}): MenuIconResolution {
    val dict = templates.get(ItemTemplateTypes.MENU_ICON_DICT)?.delegate ?: MenuIconDictionary()
    val config = MenuIconLore.LineConfig.Builder(dict).apply(dsl).build()
    val resolvedName = templates.get(ItemTemplateTypes.MENU_ICON_NAME)?.resolve(config.getTagResolver())
    val resolvedLore = templates.get(ItemTemplateTypes.MENU_ICON_LORE)?.resolve(config).orEmpty()
    // TODO 还需要解析 item_model, tooltip_style. 等资源包重构完后再写
    return MenuIconResolution(resolvedName, resolvedLore)
}
//</editor-fold>

var NekoStack.customModelData: Int? by mapped(ItemComponentTypes.CUSTOM_MODEL_DATA, ::CustomModelData, CustomModelData::data)

var NekoStack.customName: Component? by direct(ItemComponentTypes.CUSTOM_NAME)

var NekoStack.itemName: Component? by direct(ItemComponentTypes.ITEM_NAME)

var NekoStack.lore: List<Component>
    get() = wrapped.lore0.orEmpty()
    set(value) {
        wrapped.lore0 = value
    }

@Deprecated("将在高版本移除")
var NekoStack.fireResistant: Unit? by direct(ItemComponentTypes.FIRE_RESISTANT)

var NekoStack.level: Int by mapped(ItemComponentTypes.LEVEL, ItemLevel::minimumLevel, ::ItemLevel, ItemLevel::level)

var NekoStack.rarity: Rarity by mapped(ItemComponentTypes.RARITY, RarityRegistry::DEFAULT, ::ItemRarity, ItemRarity::rarity)

var NekoStack.elements: Set<Element> by mapped(ItemComponentTypes.ELEMENTS, ::emptySet, ::ItemElements, ItemElements::elements)

var NekoStack.kizamiz: Set<Kizami> by mapped(ItemComponentTypes.KIZAMIZ, ::emptySet, ::ItemKizamiz, ItemKizamiz::kizamiz)

var NekoStack.reforgeHistory: ReforgeHistory by direct(ItemComponentTypes.REFORGE_HISTORY, ReforgeHistory.ZERO)

var NekoStack.cells: ItemCells? by direct(ItemComponentTypes.CELLS)

var NekoStack.standaloneCell: StandaloneCell? by direct(ItemComponentTypes.STANDALONE_CELL)

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
        return thisRef.components.getOrDefault(type, box(def())).let(unbox)
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

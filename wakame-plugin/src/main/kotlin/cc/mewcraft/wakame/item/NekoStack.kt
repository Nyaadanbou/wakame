package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.util.MojangStack
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A wrapper of an ItemStack, which provides dedicated properties and
 * functions to manipulate wakame data on the ItemStack.
 *
 * This is a top-level interface. Except some generic use cases, you
 * probably will not directly work with this interface. Instead, you
 * will likely use the subclasses. Use your IDE to navigate them.
 */
interface NekoStack : Examinable {

    /**
     * 包含了获取特殊 [NekoStack] 实例的函数.
     */
    companion object {

        /**
         * 获取一个空的 [NekoStack], 底层为 `minecraft:air`.
         *
         * 该实例的所有状态都不可变; 尝试改变其状态不会有任何效果.
         */
        @Contract(pure = true)
        fun empty(): NekoStack {
            return EmptyNekoStack
        }
    }

    /**
     * 检查该物品是否为空.
     *
     * 空物品没有任何作用, 也不会真的出现在世界状态中.
     */
    val isEmpty: Boolean

    /**
     * 获取底层物品的类型.
     */
    val itemType: Material

    /**
     * 获取底层的 [MojangStack] 直接实例.
     * 任何对该对象的修改都会直接影响到物品本身的状态.
     */
    val mojangStack: MojangStack

    /**
     * 获取底层的 [ItemStack] 直接实例.
     * 任何对该对象的修改都会直接影响到物品本身的状态.
     */
    val bukkitStack: ItemStack

    /**
     * The namespaced identifier of this item.
     *
     * The `namespace` is the name of the directory in which the item is defined in the config.
     * The `path` is the name of the file (w/o extension) in which the item is defined in the config.
     */
    val id: Key

    /**
     * The variant of this item.
     */
    var variant: Int

    /**
     * The inventory slot group where this item becomes effective.
     */
    val slotGroup: ItemSlotGroup

    /**
     * The corresponding [NekoItem] this stack is generated from.
     */
    val prototype: NekoItem

    /**
     * 存放该物品的`物品组件`的容器. 该对象用于操作该物品的组件信息.
     */
    val components: ItemComponentMap

    /**
     * 存放该物品的`物品模板`的容器. 该对象用于读取物品模板的信息.
     */
    val templates: ItemTemplateMap

    /**
     * 存放物品的`物品行为`的容器. 该对象用于获取该物品的行为逻辑.
     */
    val behaviors: ItemBehaviorMap

    /**
     * 返回一个克隆.
     */
    fun clone(): NekoStack

    /**
     * Removes all Koish data from the item.
     */
    fun erase()

    //<editor-fold desc="Fast access to call ItemBehavior functions">
    fun handleLeftClick(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemLeftClickEvent) {
        behaviors.forEach { it.handleLeftClick(player, itemStack, koishStack, event) }
    }

    fun handleRightClick(player: Player, itemStack: ItemStack, koishStack: NekoStack, clickHand: PlayerItemRightClickEvent.Hand, event: PlayerItemRightClickEvent) {
        behaviors.forEach { it.handleRightClick(player, itemStack, koishStack, clickHand, event) }
    }

    fun handleInteract(player: Player, itemStack: ItemStack, koishStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        behaviors.forEach { it.handleInteract(player, itemStack, koishStack, action, wrappedEvent) }
    }

    fun handleInteractAtEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
        behaviors.forEach { it.handleInteractAtEntity(player, itemStack, koishStack, clicked, event) }
    }

    fun handleAttackEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, damagee: Entity, event: PostprocessDamageEvent) {
        behaviors.forEach { it.handleAttackEntity(player, itemStack, koishStack, damagee, event) }
    }

    fun handleItemProjectileLaunch(player: Player, itemStack: ItemStack, koishStack: NekoStack, projectile: Projectile, event: ProjectileLaunchEvent) {
        behaviors.forEach { it.handleItemProjectileLaunch(player, itemStack, koishStack, projectile, event) }
    }

    fun handleItemProjectileHit(player: Player, itemStack: ItemStack, koishStack: NekoStack, projectile: Projectile, event: ProjectileHitEvent) {
        behaviors.forEach { it.handleItemProjectileHit(player, itemStack, koishStack, projectile, event) }
    }

    fun handleBreakBlock(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: BlockBreakEvent) {
        behaviors.forEach { it.handleBreakBlock(player, itemStack, koishStack, event) }
    }

    fun handleDamage(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemDamageEvent) {
        behaviors.forEach { it.handleDamage(player, itemStack, koishStack, event) }
    }

    fun handleBreak(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemBreakEvent) {
        behaviors.forEach { it.handleBreak(player, itemStack, koishStack, event) }
    }

    fun handleEquip(player: Player, itemStack: ItemStack, koishStack: NekoStack, equipped: Boolean, event: ArmorChangeEvent) {
        behaviors.forEach { it.handleEquip(player, itemStack, koishStack, equipped, event) }
    }

    fun handleInventoryClick(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: InventoryClickEvent) {
        behaviors.forEach { it.handleInventoryClick(player, itemStack, koishStack, event) }
    }

    fun handleInventoryClickOnCursor(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: InventoryClickEvent) {
        behaviors.forEach { it.handleInventoryClickOnCursor(player, itemStack, koishStack, event) }
    }

    fun handleInventoryHotbarSwap(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: InventoryClickEvent) {
        behaviors.forEach { it.handleInventoryHotbarSwap(player, itemStack, koishStack, event) }
    }

    fun handleRelease(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerStopUsingItemEvent) {
        behaviors.forEach { it.handleRelease(player, itemStack, koishStack, event) }
    }

    fun handleConsume(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemConsumeEvent) {
        behaviors.forEach { it.handleConsume(player, itemStack, koishStack, event) }
    }
    //</editor-fold>
}

/**
 * 用于创建 [NekoStack] 委托的工具类.
 */
object NekoStackDelegates {

    /**
     * 创建一个只读的 [NekoStack] 委托.
     *
     * 当委托对象被访问时, 返回的对象是原始对象的克隆.
     */
    fun copyOnRead(stack: NekoStack): ReadOnlyProperty<Any?, NekoStack> {
        return CopyOnRead(stack)
    }

    /**
     * 创建一个可空的只读 [NekoStack] 委托.
     *
     * 当委托对象被访问时, 返回的对象是原始对象的克隆.
     */
    fun nullableCopyOnRead(stack: NekoStack?): ReadOnlyProperty<Any?, NekoStack?> {
        return NullableCopyOnRead(stack)
    }

    /**
     * 创建一个支持读写的 [NekoStack] 委托.
     *
     * 当委托对象被读取时, 返回的对象是原始对象的克隆.
     * 当委托对象被写入时, 会先将新的对象克隆, 再写入.
     */
    fun copyOnWrite(stack: NekoStack): ReadWriteProperty<Any?, NekoStack> {
        return CopyOnWrite(stack)
    }

    /**
     * 创建一个可空的支持读写的 [NekoStack] 委托.
     *
     * 当委托对象被读取时, 返回的对象是原始对象的克隆.
     * 当委托对象被写入时, 会先将新的对象克隆, 再写入.
     */
    fun nullableCopyOnWrite(stack: NekoStack?): ReadWriteProperty<Any?, NekoStack?> {
        return NullableCopyOnWrite(stack)
    }

    //<editor-fold desc="Implementations">
    private class CopyOnRead(
        private val stack: NekoStack,
    ) : ReadOnlyProperty<Any?, NekoStack> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): NekoStack {
            return stack.clone()
        }
    }

    private class NullableCopyOnRead(
        private val stack: NekoStack?,
    ) : ReadOnlyProperty<Any?, NekoStack?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): NekoStack? {
            return stack?.clone()
        }
    }

    private class CopyOnWrite(
        private var stack: NekoStack,
    ) : ReadWriteProperty<Any?, NekoStack> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): NekoStack {
            return stack.clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: NekoStack) {
            stack = value.clone()
        }
    }

    private class NullableCopyOnWrite(
        private var stack: NekoStack?,
    ) : ReadWriteProperty<Any?, NekoStack?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): NekoStack? {
            return stack?.clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: NekoStack?) {
            stack = value?.clone()
        }
    }
    //</editor-fold>
}

internal object EmptyNekoStack : NekoStack {
    override val isEmpty: Boolean = true

    override val itemType: Material = Material.AIR

    override val mojangStack: MojangStack
        get() = MojangStack.EMPTY

    override val bukkitStack: ItemStack
        get() = ItemStack.empty()

    override val id: Key = GenericKeys.EMPTY

    override var variant: Int
        get() = 0
        set(_) {}

    override val prototype: NekoItem = NekoItem.empty()

    override val slotGroup: ItemSlotGroup = prototype.slotGroup

    override val components: ItemComponentMap = ItemComponentMap.empty()

    override val templates: ItemTemplateMap = prototype.templates

    override val behaviors: ItemBehaviorMap = prototype.behaviors

    override fun clone(): NekoStack = this

    override fun erase() = Unit
}
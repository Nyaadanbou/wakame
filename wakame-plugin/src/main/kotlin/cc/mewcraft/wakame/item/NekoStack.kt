package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentMaps
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.skill2.Skill
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
        const val CLIENT_SIDE_KEY = "client_side"

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
     * 记录了该物品的样子是否仅存在于客户端.
     *
     * 如果为 `true`, 法宝渲染系统将修改此物品.
     * 如果为 `false`, 发包渲染系统将不会修改此物品.
     */
    var isClientSide: Boolean

    /**
     * 获取底层物品的类型.
     */
    val itemType: Material

    /**
     * 获取底层 [ItemStack] 的克隆.
     */
    @get:Contract(" -> new")
    val itemStack: ItemStack

    /**
     * 获取底层 [ItemStack] 的直接引用.
     * 任何对该对象的修改都会直接影响到物品本身的状态.
     * 除非你很清楚这么做的所有后果, 否则不要使用这个.
     */
    @get:Contract(" -> this")
    val wrapped: ItemStack

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
     * 不安全的操作. 使用前请先阅读文档.
     */
    val unsafe: Unsafe

    /**
     * 返回一个克隆.
     */
    fun clone(): NekoStack

    /**
     * Removes all the custom tags about `wakame` from the item.
     *
     * This will make the item a vanilla item, where [ItemStack.isNeko] returns `false`.
     */
    fun erase()

    fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        behaviors.forEach { it.handleInteract(player, itemStack, action, wrappedEvent) }
    }

    fun handleInteractAtEntity(player: Player, itemStack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
        behaviors.forEach { it.handleInteractAtEntity(player, itemStack, clicked, event) }
    }

    fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
        behaviors.forEach { it.handleAttackEntity(player, itemStack, damagee, event) }
    }

    fun handleItemProjectileLaunch(player: Player, itemStack: ItemStack, projectile: Projectile, event: ProjectileLaunchEvent) {
        behaviors.forEach { it.handleItemProjectileLaunch(player, itemStack, projectile, event) }
    }

    fun handleItemProjectileHit(player: Player, itemStack: ItemStack, projectile: Projectile, event: ProjectileHitEvent) {
        behaviors.forEach { it.handleItemProjectileHit(player, itemStack, projectile, event) }
    }

    fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) {
        behaviors.forEach { it.handleBreakBlock(player, itemStack, event) }
    }

    fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
        behaviors.forEach { it.handleDamage(player, itemStack, event) }
    }

    fun handleBreak(player: Player, itemStack: ItemStack, event: PlayerItemBreakEvent) {
        behaviors.forEach { it.handleBreak(player, itemStack, event) }
    }

    fun handleEquip(player: Player, itemStack: ItemStack, equipped: Boolean, event: ArmorChangeEvent) {
        behaviors.forEach { it.handleEquip(player, itemStack, equipped, event) }
    }

    fun handleInventoryClick(player: Player, itemStack: ItemStack, event: InventoryClickEvent) {
        behaviors.forEach { it.handleInventoryClick(player, itemStack, event) }
    }

    fun handleInventoryClickOnCursor(player: Player, itemStack: ItemStack, event: InventoryClickEvent) {
        behaviors.forEach { it.handleInventoryClickOnCursor(player, itemStack, event) }
    }

    fun handleInventoryHotbarSwap(player: Player, itemStack: ItemStack, event: InventoryClickEvent) {
        behaviors.forEach { it.handleInventoryHotbarSwap(player, itemStack, event) }
    }

    fun handleRelease(player: Player, itemStack: ItemStack, event: PlayerStopUsingItemEvent) {
        behaviors.forEach { it.handleRelease(player, itemStack, event) }
    }

    fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
        behaviors.forEach { it.handleConsume(player, itemStack, event) }
    }

    fun handleSkillPrepareCast(caster: Player, itemStack: ItemStack, skill: Skill, event: PlayerSkillPrepareCastEvent) {
        behaviors.forEach { it.handleSkillPrepareCast(caster, itemStack, skill, event) }
    }

    /**
     * 封装了一些“不安全”的操作.
     */
    interface Unsafe {
        /**
         * 获取该 [NekoStack] 的 `wakame` NBT 标签.
         *
         * 这是 `wakame` NBT 的直接引用, 任何对该对象的修改都会直接影响到物品本身的 NBT 数据.
         */
        val nekooTag: CompoundTag
    }
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


/* Internals */


private object EmptyNekoStack : NekoStack {
    override val isEmpty: Boolean = true

    override var isClientSide: Boolean
        get() = false // 空物品不应该渲染
        set(_) {}

    override val itemType: Material = Material.AIR

    override val itemStack: ItemStack
        get() = ItemStack.empty()

    override val wrapped: ItemStack
        get() = ItemStack.empty()

    override val id: Key = GenericKeys.EMPTY

    override var variant: Int
        get() = 0
        set(_) {}

    override val prototype: NekoItem = NekoItem.empty()

    override val slotGroup: ItemSlotGroup = prototype.slotGroup

    override val components: ItemComponentMap = ItemComponentMaps.empty()

    override val templates: ItemTemplateMap = prototype.templates

    override val behaviors: ItemBehaviorMap = prototype.behaviors

    override val unsafe: NekoStack.Unsafe = Unsafe

    override fun clone(): NekoStack {
        return this
    }

    override fun erase() {
        // do nothing
    }

    object Unsafe : NekoStack.Unsafe {
        override val nekooTag: CompoundTag
            get() = CompoundTag.create()
    }
}
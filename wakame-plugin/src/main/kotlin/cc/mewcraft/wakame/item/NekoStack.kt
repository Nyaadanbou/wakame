package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
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
interface NekoStack : Keyed, Examinable {

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
     * Gets the clone of the wrapped [ItemStack].
     */
    @get:Contract(" -> new")
    val itemStack: ItemStack

    /**
     * The `namespace` of this item.
     *
     * The `namespace` is the name of the directory in which the item is defined in the config.
     */
    val namespace: String

    /**
     * The `path` of this item.
     *
     * The `path` is the name of the file in which the item is defined in the config.
     */
    val path: String

    /**
     * The namespaced identifier of this item.
     */
    override val key: Key

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
     * ‘不安全’操作. 使用前请先阅读文档.
     */
    val unsafe: Unsafe

    /**
     * 返回一个克隆.
     */
    fun clone(): NekoStack

    /**
     * Removes all the custom tags from the item.
     *
     * This will make the item a vanilla item, where [ItemStack.isNeko] returns `false`.
     */
    fun erase()

    /**
     * 封装了一些“不安全”的操作.
     */
    interface Unsafe {
        /**
         * 获取该 [NekoStack] 的 `wakame` NBT 标签.
         *
         * 这是 `wakame` NBT 的直接引用, 任何对该对象的修改都会直接影响到物品本身的 NBT 数据.
         */
        val nbt: CompoundTag

        /**
         * 获取该 [NekoStack] 封装的 [ItemStack].
         *
         * 这是 [ItemStack] 对象的直接引用, 任何对该对象的修改都会直接影响到物品本身的状态.
         */
        val handle: ItemStack
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

    override val itemStack: ItemStack
        get() = ItemStack.empty()

    override val key: Key = GenericKeys.EMPTY

    override val namespace: String = key.namespace()

    override val path: String = key.value()

    override var variant: Int
        get() = 0
        set(_) {}

    override val prototype: NekoItem = NekoItem.empty()

    override val slotGroup: ItemSlotGroup = prototype.slotGroup

    override val components: ItemComponentMap = ItemComponentMap.empty()

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
        override val nbt: CompoundTag
            get() = CompoundTag.create()
        override val handle: ItemStack
            get() = ItemStack.empty()
    }
}
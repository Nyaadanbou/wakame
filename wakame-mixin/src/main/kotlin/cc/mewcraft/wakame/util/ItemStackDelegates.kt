package cc.mewcraft.wakame.util

import org.bukkit.inventory.ItemStack
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 用于创建 [ItemStack] 委托的工具类.
 */
object ItemStackDelegates {

    /**
     * 创建一个只读的 [ItemStack] 委托.
     *
     * 当委托对象被访问时, 返回的对象是原始对象的克隆.
     */
    fun copyOnRead(stack: ItemStack): ReadOnlyProperty<Any?, ItemStack> {
        return CopyOnRead(stack)
    }

    /**
     * 创建一个可空的只读 [ItemStack] 委托.
     *
     * 当委托对象被访问时, 返回的对象是原始对象的克隆.
     */
    fun nullableCopyOnRead(stack: ItemStack?): ReadOnlyProperty<Any?, ItemStack?> {
        return NullableCopyOnRead(stack)
    }

    /**
     * 创建一个支持读写的 [ItemStack] 委托.
     *
     * 当委托对象被读取时, 返回的对象是原始对象的克隆.
     * 当委托对象被写入时, 会先将新的对象克隆, 再写入.
     */
    fun copyOnWrite(stack: ItemStack): ReadWriteProperty<Any?, ItemStack> {
        return CopyOnWrite(stack)
    }

    /**
     * 创建一个可空的支持读写的 [ItemStack] 委托.
     *
     * 当委托对象被读取时, 返回的对象是原始对象的克隆.
     * 当委托对象被写入时, 会先将新的对象克隆, 再写入.
     */
    fun nullableCopyOnWrite(stack: ItemStack?): ReadWriteProperty<Any?, ItemStack?> {
        return NullableCopyOnWrite(stack)
    }

    //<editor-fold desc="Implementations">
    private class CopyOnRead(
        private val stack: ItemStack,
    ) : ReadOnlyProperty<Any?, ItemStack> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack {
            return stack.clone()
        }
    }

    private class NullableCopyOnRead(
        private val stack: ItemStack?,
    ) : ReadOnlyProperty<Any?, ItemStack?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack? {
            return stack?.clone()
        }
    }

    private class CopyOnWrite(
        private var stack: ItemStack,
    ) : ReadWriteProperty<Any?, ItemStack> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack {
            return stack.clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ItemStack) {
            stack = value.clone()
        }
    }

    private class NullableCopyOnWrite(
        private var stack: ItemStack?,
    ) : ReadWriteProperty<Any?, ItemStack?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack? {
            return stack?.clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ItemStack?) {
            stack = value?.clone()
        }
    }
    //</editor-fold>
}
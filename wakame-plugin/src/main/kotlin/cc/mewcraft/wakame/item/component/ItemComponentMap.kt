package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.registry.ItemComponentRegistry
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.unsafeNyaTagOrThrow
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import org.bukkit.inventory.ItemStack as BukkitStack

data class TypedItemComponent<T>(
    val type: ItemComponentType<T>,
    val value: T,
)

/**
 * 这是一个从 [ItemComponentType] 映射到具体物品组件数据的容器.
 *
 * 你可以使用该接口, 获取, 修改, 以及移除一个物品的组件数据.
 *
 * ## 不可变数据
 * 需要注意的是, 本接口返回的所有组件数据都只是当时的一个快照.
 * 修改这些组件数据的快照并不会使得改动应用到游戏内的物品之上.
 *
 * 返回快照的函数包括但不限于以下函数:
 * - [get]
 * - [getOrDefault]
 * - [getTyped]
 *
 * 如果你想修改一个组件的数据, 则需要创建一个新的快照, 然后调用以下函数:
 * - [set]
 *
 * 如果想移除一个组件, 调用以下函数:
 * - [unset]
 */
interface ItemComponentMap : Iterable<TypedItemComponent<*>>, Examinable {

    companion object {
        const val TAG_COMPONENTS = "components"

        /**
         * 获取一个空的 [ItemComponentMap] (不可变).
         */
        fun empty(): ItemComponentMap {
            return Empty
        }

        /**
         * 组合两个 [ItemComponentMap], 一个作为默认值, 一个作为对默认值的重写.
         */
        fun composite(base: ItemComponentMap, overrides: ItemComponentMap): ItemComponentMap {
            return object : ItemComponentMap {
                override fun <T> get(type: ItemComponentType<out T>): T? {
                    return overrides.get(type) ?: base.get(type)
                }

                override fun has(type: ItemComponentType<*>): Boolean {
                    return overrides.has(type) || base.has(type)
                }

                override fun <T> set(type: ItemComponentType<in T>, value: T) {
                    overrides.set(type, value)
                }

                override fun unset(type: ItemComponentType<*>) {
                    overrides.unset(type)
                }

                override fun keySet(): Set<ItemComponentType<*>> {
                    return base.keySet() union overrides.keySet()
                }

                override fun size(): Int {
                    return keySet().size
                }

                override fun fuzzySize(): Int {
                    return size()
                }

                override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
                    ExaminableProperty.of("base", base),
                    ExaminableProperty.of("overrides", overrides)
                )

                override fun toString(): String {
                    return toSimpleString()
                }
            }
        }

        /**
         * 封装一个 [BukkitStack].
         *
         * @param stack 物品堆叠
         */
        fun wrapStack(stack: BukkitStack): ItemComponentMap {
            return ForBukkitStack(stack)
        }

        /**
         * 构建一个只允许读操作的 [ItemComponentMap].
         */
        fun unmodifiable(delegate: ItemComponentMap): ItemComponentMap {
            return ForImmutable(delegate)
        }

        /**
         * 从零构建一个 [ItemComponentMap], 用于单元测试.
         */
        fun builder(): Builder {
            return Builder()
        }
    }

    fun <T> get(type: ItemComponentType<out T>): T?

    fun <T> getOrDefault(type: ItemComponentType<out T>, fallback: T): T {
        return get(type) ?: fallback
    }

    fun <T> getTyped(type: ItemComponentType<T>): TypedItemComponent<T>? {
        val value = get(type) ?: return null
        return TypedItemComponent(type, value)
    }

    fun has(type: ItemComponentType<*>): Boolean

    fun <T> set(type: ItemComponentType<in T>, value: T)

    fun unset(type: ItemComponentType<*>)

    fun filter(predicate: (ItemComponentType<*>) -> Boolean): ItemComponentMap {
        return object : ItemComponentMap {
            override fun <T> get(type: ItemComponentType<out T>): T? {
                if (predicate(type)) {
                    return this@ItemComponentMap.get(type)
                }
                return null
            }

            override fun has(type: ItemComponentType<*>): Boolean {
                if (predicate(type)) {
                    return this@ItemComponentMap.has(type)
                }
                return false
            }

            override fun <T> set(type: ItemComponentType<in T>, value: T) {
                this@ItemComponentMap.set(type, value)
            }

            override fun unset(type: ItemComponentType<*>) {
                this@ItemComponentMap.unset(type)
            }

            override fun keySet(): Set<ItemComponentType<*>> {
                return this@ItemComponentMap.keySet().filter(predicate).toSet()
            }

            override fun size(): Int {
                return this.keySet().size
            }

            override fun fuzzySize(): Int {
                return this@ItemComponentMap.fuzzySize()
            }

            override fun examinableProperties(): Stream<out ExaminableProperty> {
                return this@ItemComponentMap.examinableProperties()
            }

            override fun toString(): String {
                return this@ItemComponentMap.toString()
            }
        }
    }

    fun keySet(): Set<ItemComponentType<*>>

    fun size(): Int

    fun fuzzySize(): Int

    fun isEmpty(): Boolean {
        return this.size() == 0
    }

    override fun iterator(): Iterator<TypedItemComponent<*>> {
        return keySet().mapNotNull { getTyped(it) }.iterator()
    }

    // Classes

    /**
     * 用于构建一个直接引用的 [ItemComponentMap].
     */
    class Builder : Examinable {
        private val map: MutableMap<ItemComponentType<*>, Any> = mutableMapOf()

        fun <T> set(type: ItemComponentType<T>, value: T?): Builder {
            if (value != null) {
                map[type] = value
            } else {
                map.remove(type)
            }
            return this
        }

        fun addAll(map: ItemComponentMap): Builder {
            for (component: TypedItemComponent<*> in map) {
                this.map[component.type] = component.value as Any
            }
            return this
        }

        fun build(): ItemComponentMap {
            return ForTest(map)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map)
        )

        override fun toString(): String {
            return toSimpleString()
        }
    }

    /**
     * 空的 [ItemComponentMap] 实例 (不可变).
     */
    private object Empty : ItemComponentMap {
        override fun <T> get(type: ItemComponentType<out T>): T? = null
        override fun has(type: ItemComponentType<*>): Boolean = false
        override fun <T> set(type: ItemComponentType<in T>, value: T) = Unit
        override fun unset(type: ItemComponentType<*>) = Unit
        override fun keySet(): Set<ItemComponentType<*>> = emptySet()
        override fun size(): Int = 0
        override fun fuzzySize(): Int = 0
        override fun iterator(): Iterator<TypedItemComponent<*>> = emptySet<TypedItemComponent<*>>().iterator()
        override fun toString(): String = toSimpleString()
    }

    /**
     * 本实现直接储存了所有组件, 不涉及任何封装和 NBT 操作.
     */
    private class ForTest(
        private val map: MutableMap<ItemComponentType<*>, Any>,
    ) : ItemComponentMap {
        override fun <T> get(type: ItemComponentType<out T>): T? {
            @Suppress("UNCHECKED_CAST")
            return map[type] as T?
        }

        override fun has(type: ItemComponentType<*>): Boolean {
            return map.containsKey(type)
        }

        override fun <T> set(type: ItemComponentType<in T>, value: T) {
            map[type] = value as Any
        }

        override fun unset(type: ItemComponentType<*>) {
            map.remove(type)
        }

        override fun size(): Int {
            return map.size
        }

        override fun fuzzySize(): Int {
            return size()
        }

        override fun keySet(): Set<ItemComponentType<*>> {
            return map.keys
        }

        override fun iterator(): Iterator<TypedItemComponent<*>> {
            return map.entries.mapNotNull { getTyped(it.key) }.iterator()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map)
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other is ForTest && this.map == other.map) return true
            return false
        }

        override fun hashCode(): Int {
            return map.hashCode()
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    /**
     * 封装了一个 [BukkitStack].
     *
     * 所有组件的*读取*将从封装的物品上获取;
     * 所有组件的*写入*将应用到封装的物品上.
     */
    private class ForBukkitStack(
        private val stack: BukkitStack,
    ) : ItemComponentMap {
        override fun <T> get(type: ItemComponentType<out T>): T? {
            return type.read(ItemComponentHolder.create(stack))
        }

        override fun has(type: ItemComponentType<*>): Boolean {
            return get(type) != null
        }

        override fun <T> set(type: ItemComponentType<in T>, value: T) {
            type.write(ItemComponentHolder.create(stack), value)
        }

        override fun unset(type: ItemComponentType<*>) {
            type.remove(ItemComponentHolder.create(stack))
        }

        override fun keySet(): Set<ItemComponentType<*>> {
            return stack
                .unsafeNyaTagOrThrow
                .getCompoundOrNull(TAG_COMPONENTS)
                ?.keySet()
                ?.mapNotNull(ItemComponentRegistry.TYPES::find)
                ?.let(::ReferenceArraySet)
                .orEmpty()
        }

        override fun size(): Int {
            return keySet().size
        }

        override fun fuzzySize(): Int {
            return stack.unsafeNyaTagOrThrow.getCompoundOrNull(TAG_COMPONENTS)?.size() ?: 0
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("stack", stack),
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other is ForBukkitStack && this.stack == other.stack) return true
            return false
        }

        override fun hashCode(): Int {
            return stack.hashCode()
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    /**
     * 本质是个委托, 用于构建一个只读的 [ItemComponentMap].
     */
    private class ForImmutable(
        private val delegate: ItemComponentMap,
    ) : ItemComponentMap {
        override fun <T> get(type: ItemComponentType<out T>): T? {
            return delegate.get(type)
        }

        override fun has(type: ItemComponentType<*>): Boolean {
            return delegate.has(type)
        }

        override fun <T> set(type: ItemComponentType<in T>, value: T) {
            throw UnsupportedOperationException("Write operations are not allowed in this map")
        }

        override fun unset(type: ItemComponentType<*>) {
            throw UnsupportedOperationException("Write operations are not allowed in this map")
        }

        override fun keySet(): Set<ItemComponentType<*>> {
            return delegate.keySet()
        }

        override fun size(): Int {
            return delegate.size()
        }

        override fun fuzzySize(): Int {
            return delegate.fuzzySize()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return delegate.examinableProperties()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return delegate == other
        }

        override fun hashCode(): Int {
            return delegate.hashCode()
        }

        override fun toString(): String {
            return delegate.toString()
        }
    }
}

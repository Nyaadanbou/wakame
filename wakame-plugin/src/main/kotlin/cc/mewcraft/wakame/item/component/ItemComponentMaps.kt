package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.component.ItemComponentMap.Builder
import cc.mewcraft.wakame.item.component.ItemComponentMap.Companion.TAG_COMPONENTS
import cc.mewcraft.wakame.util.*
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import org.bukkit.inventory.ItemStack as BukkitStack

internal object ItemComponentMaps {
    /**
     * 获取一个空的 [ItemComponentMap] (不可变).
     */
    fun empty(): ItemComponentMap {
        return EmptyItemComponentMap
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
        return WrappingItemComponentMap(stack)
    }

    /**
     * 构建一个只允许读操作的 [ItemComponentMap].
     */
    fun unmodifiable(delegate: ItemComponentMap): ItemComponentMap {
        return ImmutableItemComponentMap(delegate)
    }

    /**
     * 从零构建一个 [ItemComponentMap], 用于单元测试.
     */
    fun builder(): Builder {
        return ItemComponentMapBuilderImpl()
    }
}


/* Privates */


/**
 * [ItemComponentMap.Builder] 的实现.
 */
private class ItemComponentMapBuilderImpl : Builder {
    private val map: MutableMap<ItemComponentType<*>, Any> = mutableMapOf()

    override fun <T> set(type: ItemComponentType<T>, value: T?): Builder {
        if (value != null) {
            map[type] = value
        } else {
            map.remove(type)
        }
        return this
    }

    override fun addAll(map: ItemComponentMap): Builder {
        for (component: TypedItemComponent<*> in map) {
            this.map[component.type] = component.value as Any
        }
        return this
    }

    override fun build(): ItemComponentMap {
        return SimpleItemComponentMap(map)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("map", map)
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 空的 [ItemComponentMap] 实现. 不可变.
 */
private object EmptyItemComponentMap : ItemComponentMap {
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
 * 标准的 [ItemComponentMap] 实现.
 *
 * 本实现直接把储存组件的数据储存在 [map], 不涉及封装操作.
 */
private class SimpleItemComponentMap(
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
        if (other is SimpleItemComponentMap && this.map == other.map) return true
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
 * 该实现用于封装一个 [BukkitStack], 也就是说:
 * - 所有组件的*读取*将从封装的物品上获取;
 * - 所有组件的*写入*将应用到封装的物品上.
 */
private class WrappingItemComponentMap(
    private val stack: BukkitStack,
) : ItemComponentMap {
    override fun <T> get(type: ItemComponentType<out T>): T? {
        return type.read(ItemComponentHolders.create(stack))
    }

    override fun has(type: ItemComponentType<*>): Boolean {
        return get(type) != null
    }

    override fun <T> set(type: ItemComponentType<in T>, value: T) {
        type.write(ItemComponentHolders.create(stack), value)
    }

    override fun unset(type: ItemComponentType<*>) {
        type.remove(ItemComponentHolders.create(stack))
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
        if (other is WrappingItemComponentMap && this.stack == other.stack) return true
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
 * 该实现本质是个委托, 用于构建一个只读的 [ItemComponentMap].
 */
private class ImmutableItemComponentMap(
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
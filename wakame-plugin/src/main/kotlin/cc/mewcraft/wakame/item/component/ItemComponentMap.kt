package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.KoishStackImplementations
import cc.mewcraft.wakame.item.component.ItemComponentMap.Builder
import cc.mewcraft.wakame.item.component.ItemComponentMap.Companion.COMPONENTS_FIELD
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.data.getCompoundOrNull
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.Collections.emptySet
import java.util.stream.Stream

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
// TODO 重命名:
//  ItemComponentBridge -> ItemDataBridge
//  ItemComponentConfig -> ItemDataConfig
//  ItemComponentHolder -> ItemDataAccess
//  ItemComponentMap -> ItemDataContainer
//  ItemComponentRegistry -> 配合 Registry 重构
//  ItemComponentType -> ItemDataType
//  ItemComponentTypes -> ItemDataTypes
interface ItemComponentMap : Iterable<TypedItemComponent<*>>, Examinable {

    companion object {
        const val COMPONENTS_FIELD = "components"

        /**
         * 获取一个空的 [ItemComponentMap] (不可变).
         */
        fun empty(): ItemComponentMap {
            return EmptyItemComponentMap
        }

        /**
         * 从零构建一个 [ItemComponentMap], 用于单元测试.
         */
        fun builder(): Builder {
            return ItemComponentMapBuilderImpl()
        }

        /**
         * 组合两个 [ItemComponentMap], 一个作为默认值, 一个作为对默认值的重写.
         */
        fun compose(base: ItemComponentMap, overrides: ItemComponentMap): ItemComponentMap {
            return object : ItemComponentMap {
                override fun <T> get(type: ItemComponentType<out T>): T? = overrides.get(type) ?: base.get(type)
                override fun has(type: ItemComponentType<*>): Boolean = overrides.has(type) || base.has(type)
                override fun <T> set(type: ItemComponentType<in T>, value: T) = overrides.set(type, value)
                override fun unset(type: ItemComponentType<*>) = overrides.unset(type)
                override fun keySet(): Set<ItemComponentType<*>> = base.keySet() union overrides.keySet()
                override fun size(): Int = keySet().size
                override fun fuzzySize(): Int = size()

                override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
                    ExaminableProperty.of("base", base),
                    ExaminableProperty.of("overrides", overrides)
                )

                override fun toString(): String = toSimpleString()
            }
        }

        /**
         * 封装一个 [MojangStack].
         *
         * @param itemstack 物品堆叠
         */
        fun mutable(itemstack: MojangStack): ItemComponentMap = WrappingItemComponentMap(itemstack)

        /**
         * 构建一个只允许读操作的 [ItemComponentMap].
         */
        fun immutable(itemstack: MojangStack): ItemComponentMap = immutable(mutable(itemstack))

        /**
         * 构建一个只允许读操作的 [ItemComponentMap].
         */
        fun immutable(delegate: ItemComponentMap): ItemComponentMap = ImmutableItemComponentMap(delegate)
    }

    fun <T> get(type: ItemComponentType<out T>): T?

    fun <T> getOrDefault(type: ItemComponentType<out T>, fallback: T): T = get(type) ?: fallback

    fun <T> getOrDefault(type: ItemComponentType<out T>, fallback: () -> T): T = get(type) ?: fallback()

    fun <T> getTyped(type: ItemComponentType<T>): TypedItemComponent<T>? = get(type)?.let { value -> TypedItemComponent(type, value) }

    fun has(type: ItemComponentType<*>): Boolean

    fun <T> set(type: ItemComponentType<in T>, value: T)

    fun unset(type: ItemComponentType<*>)

    fun filter(predicate: (ItemComponentType<*>) -> Boolean): ItemComponentMap {
        return object : ItemComponentMap {
            override fun <T> get(type: ItemComponentType<out T>): T? = if (predicate(type)) this@ItemComponentMap.get(type) else null
            override fun has(type: ItemComponentType<*>): Boolean = if (predicate(type)) this@ItemComponentMap.has(type) else false
            override fun <T> set(type: ItemComponentType<in T>, value: T) = this@ItemComponentMap.set(type, value)
            override fun unset(type: ItemComponentType<*>) = this@ItemComponentMap.unset(type)
            override fun keySet(): Set<ItemComponentType<*>> = this@ItemComponentMap.keySet().filter(predicate).toSet()
            override fun size(): Int = this.keySet().size
            override fun fuzzySize(): Int = this@ItemComponentMap.fuzzySize()
            override fun examinableProperties(): Stream<out ExaminableProperty> = this@ItemComponentMap.examinableProperties()
            override fun toString(): String = this@ItemComponentMap.toString()
        }
    }

    fun keySet(): Set<ItemComponentType<*>>
    fun size(): Int
    fun fuzzySize(): Int
    fun isEmpty(): Boolean = this.size() == 0
    override fun iterator(): Iterator<TypedItemComponent<*>> = keySet().mapNotNull { getTyped(it) }.iterator()

    /**
     * 用于构建一个直接引用的 [ItemComponentMap].
     */
    interface Builder : Examinable {
        fun <T> set(type: ItemComponentType<T>, value: T?): Builder
        fun addAll(map: ItemComponentMap): Builder
        fun build(): ItemComponentMap
    }

}

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

    override fun build(): ItemComponentMap = DirectItemComponentMap(map)
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(ExaminableProperty.of("map", map))
    override fun toString(): String = toSimpleString()

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
 * 标准的 [ItemComponentMap] 实现. 本实现直接把储存组件的数据储存在 [map].
 */
private class DirectItemComponentMap(
    private val map: MutableMap<ItemComponentType<*>, Any>,
) : ItemComponentMap {

    override fun <T> get(type: ItemComponentType<out T>): T? = map[type] as T?
    override fun has(type: ItemComponentType<*>): Boolean = map.containsKey(type)

    override fun <T> set(type: ItemComponentType<in T>, value: T) {
        map[type] = value as Any
    }

    override fun unset(type: ItemComponentType<*>) {
        map.remove(type)
    }

    override fun size(): Int = map.size
    override fun fuzzySize(): Int = size()
    override fun keySet(): Set<ItemComponentType<*>> = map.keys
    override fun iterator(): Iterator<TypedItemComponent<*>> = map.entries.mapNotNull { getTyped(it.key) }.iterator()
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(ExaminableProperty.of("map", map))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is DirectItemComponentMap && this.map == other.map) return true
        return false
    }

    override fun hashCode(): Int = map.hashCode()
    override fun toString(): String = toSimpleString()

}

/**
 * 该实现用于封装一个 [MojangStack], 也就是说:
 * - 所有组件的*读取*将从封装的物品上获取;
 * - 所有组件的*写入*将应用到封装的物品上.
 */
private class WrappingItemComponentMap(
    private val itemstack: MojangStack,
) : ItemComponentMap {

    override fun <T> get(type: ItemComponentType<out T>): T? = type.read(ItemComponentHolder.itemComponentHolder(itemstack))
    override fun has(type: ItemComponentType<*>): Boolean = get(type) != null
    override fun <T> set(type: ItemComponentType<in T>, value: T) = type.write(ItemComponentHolder.itemComponentHolder(itemstack), value)
    override fun unset(type: ItemComponentType<*>) = type.remove(ItemComponentHolder.itemComponentHolder(itemstack))
    override fun keySet(): Set<ItemComponentType<*>> = KoishStackImplementations.getNbt(itemstack)
        ?.getCompoundOrNull(COMPONENTS_FIELD)
        ?.allKeys
        ?.mapNotNull(ItemComponentRegistry.TYPES::get)
        ?.let(::ReferenceArraySet)
        .orEmpty()

    override fun size(): Int = keySet().size
    override fun fuzzySize(): Int = KoishStackImplementations.getNbt(itemstack)?.getCompoundOrNull(COMPONENTS_FIELD)?.size() ?: 0
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(ExaminableProperty.of("itemstack", itemstack))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is WrappingItemComponentMap && this.itemstack == other.itemstack) return true
        return false
    }

    override fun hashCode(): Int = itemstack.hashCode()
    override fun toString(): String = toSimpleString()

}

/**
 * 该实现本质是个委托, 用于构建一个只读的 [ItemComponentMap].
 */
private class ImmutableItemComponentMap(
    private val delegate: ItemComponentMap,
) : ItemComponentMap by delegate {

    override fun <T> set(type: ItemComponentType<in T>, value: T) =
        throwUoe()

    override fun unset(type: ItemComponentType<*>) =
        throwUoe()

    private fun throwUoe(): Nothing =
        throw UnsupportedOperationException("Write operations are not allowed in this map")

}
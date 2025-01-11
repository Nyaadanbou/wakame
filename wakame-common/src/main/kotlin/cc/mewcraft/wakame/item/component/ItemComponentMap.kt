package cc.mewcraft.wakame.item.component

import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
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
interface ItemComponentMap : Iterable<TypedItemComponent<*>>, Examinable {

    companion object {
        const val TAG_COMPONENTS = "components"
    }

    fun <T> get(type: ItemComponentType<out T>): T?

    fun <T> getOrDefault(type: ItemComponentType<out T>, fallback: T): T {
        return get(type) ?: fallback
    }

    fun <T> getOrDefault(type: ItemComponentType<out T>, fallback: () -> T): T {
        return get(type) ?: fallback()
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

    /**
     * 用于构建一个直接引用的 [ItemComponentMap].
     */
    interface Builder : Examinable {
        fun <T> set(type: ItemComponentType<T>, value: T?): Builder
        fun addAll(map: ItemComponentMap): Builder
        fun build(): ItemComponentMap
    }
}

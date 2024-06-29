package cc.mewcraft.wakame.item.component

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.registry.ItemComponentRegistry
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import cc.mewcraft.wakame.util.wakameTagOrNull
import org.bukkit.inventory.ItemStack

data class TypedItemComponent<T>(
    val type: ItemComponentType<T, *>,
    val value: T,
)

/**
 * 这是一个从 [ItemComponentType] 映射到具体物品组件数据的容器.
 *
 * 你可以使用该接口, 获取, 修改, 以及移除, 一个物品的组件数据.
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
interface ItemComponentMap : Iterable<TypedItemComponent<*>> {

    companion object {
        val EMPTY: ItemComponentMap = object : ItemComponentMap {
            override fun <T, S : ItemComponentHolder> get(type: ItemComponentType<T, S>): T? = null
            override fun has(type: ItemComponentType<*, *>): Boolean = false
            override fun <T, S : ItemComponentHolder> set(type: ItemComponentType<T, S>, value: T) = Unit
            override fun unset(type: ItemComponentType<*, *>) = Unit
            override fun keySet(): Set<ItemComponentType<*, *>> = emptySet()
            override fun size(): Int = 0
            override fun iterator(): Iterator<TypedItemComponent<*>> = emptySet<TypedItemComponent<*>>().iterator()
        }

        /**
         * 封装一个 [ItemStack].
         */
        fun wrapItem(item: ItemStack): ItemComponentMap {
            return ItemMap(item)
        }

        /**
         * 组合两个 [ItemComponentMap], 一个作为默认值, 一个作为对默认值的重写.
         */
        fun composite(base: ItemComponentMap, overrides: ItemComponentMap): ItemComponentMap {
            return object : ItemComponentMap {
                override fun <T, S : ItemComponentHolder> get(type: ItemComponentType<T, S>): T? {
                    return overrides.get(type) ?: base.get(type)
                }

                override fun has(type: ItemComponentType<*, *>): Boolean {
                    return overrides.has(type) || base.has(type)
                }

                override fun <T, S : ItemComponentHolder> set(type: ItemComponentType<T, S>, value: T) {
                    overrides.set(type, value)
                }

                override fun unset(type: ItemComponentType<*, *>) {
                    overrides.unset(type)
                }

                override fun keySet(): Set<ItemComponentType<*, *>> {
                    return base.keySet() union overrides.keySet()
                }

                override fun size(): Int {
                    return keySet().size
                }
            }
        }

        /**
         * 从零构建一个 [ItemComponentMap], 用于单元测试.
         */
        fun builder(): Builder {
            return Builder()
        }
    }

    fun <T, S : ItemComponentHolder> get(type: ItemComponentType<T, S>): T?

    fun <T, S : ItemComponentHolder> getOrDefault(type: ItemComponentType<T, S>, fallback: T): T {
        return get(type) ?: fallback
    }

    fun <T, S : ItemComponentHolder> getTyped(type: ItemComponentType<T, S>): TypedItemComponent<T>? {
        val value = get(type) ?: return null
        return TypedItemComponent(type, value)
    }

    fun has(type: ItemComponentType<*, *>): Boolean

    fun <T, S : ItemComponentHolder> set(type: ItemComponentType<T, S>, value: T)

    fun unset(type: ItemComponentType<*, *>)

    fun filter(predicate: (ItemComponentType<*, *>) -> Boolean): ItemComponentMap {
        return object : ItemComponentMap {
            override fun <T, S : ItemComponentHolder> get(type: ItemComponentType<T, S>): T? {
                if (predicate(type)) {
                    return this@ItemComponentMap.get(type)
                }
                return null
            }

            override fun has(type: ItemComponentType<*, *>): Boolean {
                if (predicate(type)) {
                    return this@ItemComponentMap.has(type)
                }
                return false
            }

            override fun <T, S : ItemComponentHolder> set(type: ItemComponentType<T, S>, value: T) {
                this@ItemComponentMap.set(type, value)
            }

            override fun unset(type: ItemComponentType<*, *>) {
                this@ItemComponentMap.unset(type)
            }

            override fun keySet(): Set<ItemComponentType<*, *>> {
                return this@ItemComponentMap.keySet().filter(predicate).toSet()
            }

            override fun size(): Int {
                return keySet().size
            }
        }
    }

    fun keySet(): Set<ItemComponentType<*, *>>

    fun size(): Int

    fun isEmpty(): Boolean {
        return this.size() == 0
    }

    override fun iterator(): Iterator<TypedItemComponent<*>> {
        return keySet().mapNotNull { getTyped(it) }.iterator()
    }

    // Classes

    class Builder {
        private val map: MutableMap<ItemComponentType<*, *>, Any> = mutableMapOf()

        fun <T> set(type: ItemComponentType<T, *>, value: T?): Builder {
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
            return buildFromMap(map)
        }

        private fun buildFromMap(components: Map<ItemComponentType<*, *>, Any>): ItemComponentMap {
            return if (components.isEmpty()) {
                EMPTY
            } else {
                SimpleMap(components.toMutableMap())
            }
        }
    }

    /**
     * 用于单元测试.
     */
    private class SimpleMap(
        private val map: MutableMap<ItemComponentType<*, *>, Any>,
    ) : ItemComponentMap {
        override fun <T, S : ItemComponentHolder> get(type: ItemComponentType<T, S>): T? {
            return map[type] as (T?)
        }

        override fun has(type: ItemComponentType<*, *>): Boolean {
            return map.containsKey(type)
        }

        override fun <T, S : ItemComponentHolder> set(type: ItemComponentType<T, S>, value: T) {
            map[type] = value as Any
        }

        override fun unset(type: ItemComponentType<*, *>) {
            map.remove(type)
        }

        override fun size(): Int {
            return map.size
        }

        override fun keySet(): Set<ItemComponentType<*, *>> {
            return map.keys
        }

        override fun iterator(): Iterator<TypedItemComponent<*>> {
            return map.entries.mapNotNull { getTyped(it.key) }.iterator()
        }

        override fun toString(): String {
            return map.toString()
        }
    }

    /**
     * 用于封装物品.
     */
    private class ItemMap(
        private val item: ItemStack,
    ) : ItemComponentMap {
        // 储存了所有组件信息的
        private val nbt: CompoundTag = item.wakameTagOrNull?.getCompoundOrNull("components") ?: throw IllegalStateException()

        override fun <T, S : ItemComponentHolder> get(type: ItemComponentType<T, S>): T? {
            val id = type.id
            val source = type.holder
            when (source) {
                ItemComponentType.Holder.NBT -> {
                    // 涉及的组件: Arrow, Attributable, Crate, ItemCells, ItemElements, ItemKizamiz, ItemLevel, ItemLore, ItemRarity, ItemSkin, ItemSkinOwner, Kizamiable, Skillful, SystemUse, Trackable

                    // 如果不存在此标签, 则代表该组件不存在, 所以直接返回 null
                    val compound = nbt.getCompoundOrNull(id) ?: return null

                    val holder = ItemComponentHolder.NBT(compound)
                    val cast = type as ItemComponentType<T, ItemComponentHolder.NBT>
                    return cast.read(holder)
                }

                ItemComponentType.Holder.ITEM -> {
                    // 涉及的组件: CustomName, CustomModelData, FireResistant, ItemDamage, ItemMaxDamage, ItemName, Tool, Unbreakable

                    val holder = ItemComponentHolder.Item(item)
                    val cast = type as ItemComponentType<T, ItemComponentHolder.Item>
                    return cast.read(holder)
                }

                ItemComponentType.Holder.COMPLEX -> {
                    // 涉及的组件: Damageable, FoodProperties

                    // 两者都需要的情况下, 如果其一不存在, 则直接返回 null
                    val compound = nbt.getCompoundOrNull(id) ?: return null

                    val holder = ItemComponentHolder.Complex(item, compound)
                    val cast = type as ItemComponentType<T, ItemComponentHolder.Complex>
                    return cast.read(holder)
                }
            }
        }

        override fun has(type: ItemComponentType<*, *>): Boolean {
            return get(type) != null
        }

        override fun <T, S : ItemComponentHolder> set(type: ItemComponentType<T, S>, value: T) {
            val id = type.id
            val source = type.holder
            when (source) {
                // 涉及的组件: 见 get()
                ItemComponentType.Holder.NBT -> {
                    val compound = nbt.getOrPut(id, CompoundTag::create)
                    val holder = ItemComponentHolder.NBT(compound)
                    val cast = type as ItemComponentType<T, ItemComponentHolder.NBT>
                    cast.write(holder, value)
                }

                ItemComponentType.Holder.ITEM -> {
                    val holder = ItemComponentHolder.Item(item)
                    val cast = type as ItemComponentType<T, ItemComponentHolder.Item>
                    cast.write(holder, value)
                }

                ItemComponentType.Holder.COMPLEX -> {
                    val compound = nbt.getOrPut(id, CompoundTag::create)
                    val holder = ItemComponentHolder.Complex(item, compound)
                    val cast = type as ItemComponentType<T, ItemComponentHolder.Complex>
                    cast.write(holder, value)
                }
            }
        }

        override fun unset(type: ItemComponentType<*, *>) {
            val id = type.id
            val source = type.holder
            when (source) {
                ItemComponentType.Holder.NBT -> {
                    nbt.remove(id)
                }

                ItemComponentType.Holder.ITEM -> {
                    val holder = ItemComponentHolder.Item(item)
                    val cast = type as ItemComponentType<*, ItemComponentHolder.Item>
                    cast.remove(holder)
                }

                ItemComponentType.Holder.COMPLEX -> {
                    // 这个 NBT 标签实际上没必要 getOrPut, 因为反正
                    // 这个标签最后要被移除. 只不过因为 Complex 要求非空.
                    // val compound = nbt.getOrPut(id, CompoundTag::create)

                    val compound = EMPTY_COMPOUND
                    val holder = ItemComponentHolder.Complex(item, compound)
                    val cast = type as ItemComponentType<*, ItemComponentHolder.Complex>
                    cast.remove(holder)
                    nbt.remove(id)
                }
            }
        }

        override fun keySet(): Set<ItemComponentType<*, *>> {
            return nbt.keySet().mapNotNull { ItemComponentRegistry.TYPES.find(it) }.toSet()
        }

        override fun size(): Int {
            return keySet().size
        }

        override fun toString(): String {
            return nbt.toString()
        }

        private companion object {
            val EMPTY_COMPOUND: CompoundTag = CompoundTag.create()
        }
    }
}

package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.registry.ItemComponentRegistry
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import cc.mewcraft.wakame.util.nekoCompoundOrNull
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.bukkit.inventory.ItemStack

sealed interface TypedItemComponent {
    val type: ItemComponentType

    data class Valued<T>(override val type: ItemComponentType, val value: T) : TypedItemComponent
    data class NonValued(override val type: ItemComponentType) : TypedItemComponent
}

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
 * - [add]
 *
 * 如果想移除一个组件, 调用以下函数:
 * - [remove]
 */
interface ItemComponentMap : Iterable<TypedItemComponent> {

    companion object {
        val EMPTY: ItemComponentMap = object : ItemComponentMap {
            override fun <T, S : ItemComponentHolder> get(type: ItemComponentType.Valued<T, S>): T? = null
            override fun has(type: ItemComponentType): Boolean = false
            override fun <T, S : ItemComponentHolder> add(type: ItemComponentType.Valued<T, S>, value: T) = Unit
            override fun <S : ItemComponentHolder> add(type: ItemComponentType.NonValued<S>) = Unit
            override fun remove(type: ItemComponentType) = Unit
            override fun keySet(): Set<ItemComponentType> = emptySet()
            override fun size(): Int = 0
            override fun iterator(): Iterator<TypedItemComponent> = emptySet<TypedItemComponent>().iterator()
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
                override fun <T, S : ItemComponentHolder> get(type: ItemComponentType.Valued<T, S>): T? {
                    return overrides.get(type) ?: base.get(type)
                }

                override fun has(type: ItemComponentType): Boolean {
                    return overrides.has(type) || base.has(type)
                }

                override fun <T, S : ItemComponentHolder> add(type: ItemComponentType.Valued<T, S>, value: T) {
                    overrides.add(type, value)
                }

                override fun <S : ItemComponentHolder> add(type: ItemComponentType.NonValued<S>) {
                    overrides.add(type)
                }

                override fun remove(type: ItemComponentType) {
                    overrides.remove(type)
                }

                override fun keySet(): Set<ItemComponentType> {
                    return base.keySet() union overrides.keySet()
                }

                override fun size(): Int {
                    return keySet().size
                }
            }
        }

        fun builder(): Builder {
            return Builder()
        }
    }

    fun <T, S : ItemComponentHolder> get(type: ItemComponentType.Valued<T, S>): T?

    fun <T, S : ItemComponentHolder> getOrDefault(type: ItemComponentType.Valued<T, S>, fallback: T): T {
        return get(type) ?: fallback
    }

    fun <T, S : ItemComponentHolder> getTyped(type: ItemComponentType.Valued<T, S>): TypedItemComponent? {
        val value = get(type) ?: return null
        return TypedItemComponent.Valued(type, value)
    }

    fun has(type: ItemComponentType): Boolean

    fun <T, S : ItemComponentHolder> add(type: ItemComponentType.Valued<T, S>, value: T)

    fun <S : ItemComponentHolder> add(type: ItemComponentType.NonValued<S>)

    fun remove(type: ItemComponentType)

    fun filter(predicate: (ItemComponentType) -> Boolean): ItemComponentMap {
        return object : ItemComponentMap {
            override fun <T, S : ItemComponentHolder> get(type: ItemComponentType.Valued<T, S>): T? {
                if (predicate(type)) {
                    return this@ItemComponentMap.get(type)
                }
                return null

            }

            override fun has(type: ItemComponentType): Boolean {
                if (predicate(type)) {
                    return this@ItemComponentMap.has(type)
                }
                return false
            }

            override fun <T, S : ItemComponentHolder> add(type: ItemComponentType.Valued<T, S>, value: T) {
                this@ItemComponentMap.add(type, value)
            }

            override fun <S : ItemComponentHolder> add(type: ItemComponentType.NonValued<S>) {
                this@ItemComponentMap.add(type)
            }

            override fun remove(type: ItemComponentType) {
                this@ItemComponentMap.remove(type)
            }

            override fun keySet(): Set<ItemComponentType> {
                return this@ItemComponentMap.keySet().filter(predicate).toSet()
            }

            override fun size(): Int {
                return keySet().size
            }
        }
    }

    fun keySet(): Set<ItemComponentType>

    fun size(): Int

    fun isEmpty(): Boolean {
        return this.size() == 0
    }

    override fun iterator(): Iterator<TypedItemComponent> {
        return keySet().mapNotNull {
            when (it) {
                is ItemComponentType.Valued<*, *> -> getTyped(it)
                is ItemComponentType.NonValued<*> -> TypedItemComponent.NonValued(it)
            }
        }.iterator()
    }

    // Classes

    class Builder {
        private val map: MutableMap<ItemComponentType, Any> = mutableMapOf()

        fun <T> set(type: ItemComponentType, value: T?): Builder {
            if (value != null) {
                map[type] = value
            } else {
                map.remove(type)
            }
            return this
        }

        fun addAll(map: ItemComponentMap): Builder {
            for (component: TypedItemComponent in map) {
                when (component) {
                    is TypedItemComponent.Valued<*> -> {
                        this.map[component.type] = component.value as Any
                    }

                    is TypedItemComponent.NonValued -> {
                        this.map[component.type] = Unit
                    }
                }
            }
            return this
        }

        fun build(): ItemComponentMap {
            return buildFromMap(map)
        }

        private fun buildFromMap(components: Map<ItemComponentType, Any>): ItemComponentMap {
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
        private val map: MutableMap<ItemComponentType, Any>,
    ) : ItemComponentMap {
        override fun <T, S : ItemComponentHolder> get(type: ItemComponentType.Valued<T, S>): T? {
            return map[type] as (T?)
        }

        override fun has(type: ItemComponentType): Boolean {
            return map.containsKey(type)
        }

        override fun <T, S : ItemComponentHolder> add(type: ItemComponentType.Valued<T, S>, value: T) {
            map[type] = value as Any
        }

        override fun <S : ItemComponentHolder> add(type: ItemComponentType.NonValued<S>) {
            map[type] = Unit
        }

        override fun remove(type: ItemComponentType) {
            map.remove(type)
        }

        override fun size(): Int {
            return map.size
        }

        override fun keySet(): Set<ItemComponentType> {
            return map.keys
        }

        override fun iterator(): Iterator<TypedItemComponent> {
            return map.entries.map { entry ->
                when (val type = entry.key) {
                    is ItemComponentType.Valued<*, *> -> {
                        TypedItemComponent.Valued(type, entry.value)
                    }

                    is ItemComponentType.NonValued<*> -> {
                        TypedItemComponent.NonValued(type)
                    }
                }
            }.iterator()
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
        private val nbt: CompoundShadowTag = item.nekoCompoundOrNull?.getCompoundOrNull("components") ?: throw IllegalStateException()

        override fun <T, S : ItemComponentHolder> get(type: ItemComponentType.Valued<T, S>): T? {
            val id = type.id
            val source = type.holder
            when (source) {
                ItemComponentType.Holder.NBT -> {
                    // 涉及的组件: Arrow, ItemCells, ItemElement, ItemKizami, ItemLevel, ItemLore, ItemRarity, ItemSkin, ItemSkinOwner, ItemStatistics

                    // 如果不存在此标签, 则代表该组件不存在, 所以直接返回 null
                    val compound = nbt.getCompoundOrNull(id) ?: return null

                    val holder = ItemComponentHolder.NBT(compound)
                    val cast = type as ItemComponentType.Valued<T, ItemComponentHolder.NBT>
                    return cast.read(holder)
                }

                ItemComponentType.Holder.ITEM -> {
                    // 涉及的组件: CustomName, ItemDamage, ItemMaxDamage, ItemName, Tool

                    val holder = ItemComponentHolder.Item(item)
                    val cast = type as ItemComponentType.Valued<T, ItemComponentHolder.Item>
                    return cast.read(holder)
                }

                ItemComponentType.Holder.COMPLEX -> {
                    // 涉及的组件: FoodProperties

                    // 两者都需要的情况下, 如果其一不存在, 则直接返回 null
                    val compound = nbt.getCompoundOrNull(id) ?: return null

                    val holder = ItemComponentHolder.Complex(item, compound)
                    val cast = type as ItemComponentType.Valued<T, ItemComponentHolder.Complex>
                    return cast.read(holder)
                }
            }
        }

        override fun has(type: ItemComponentType): Boolean {
            when (type) {
                is ItemComponentType.Valued<*, *> -> {
                    // 涉及的组件: 见函数 get()
                    return get(type) != null
                }

                is ItemComponentType.NonValued<*> -> {
                    val id = type.id
                    val source = type.holder
                    when (source) {
                        ItemComponentType.Holder.NBT -> {
                            // 涉及的组件: Attributable, Kizamiable, Skillful

                            return nbt.contains(id)
                        }

                        ItemComponentType.Holder.ITEM -> {
                            // 涉及的组件: FireResistant

                            val holder = ItemComponentHolder.Item(item)
                            val cast = type as ItemComponentType.NonValued<ItemComponentHolder.Item>
                            return cast.read(holder)
                        }

                        ItemComponentType.Holder.COMPLEX -> {
                            // 涉及的组件:

                            val compound = nbt.getCompoundOrNull(id) ?: return false
                            val holder = ItemComponentHolder.Complex(item, compound)
                            val cast = type as ItemComponentType.NonValued<ItemComponentHolder.Complex>
                            return cast.read(holder)
                        }
                    }
                }
            }
        }

        override fun <T, S : ItemComponentHolder> add(type: ItemComponentType.Valued<T, S>, value: T) {
            val id = type.id
            val source = type.holder
            when (source) {
                // 涉及的组件: 见 get()
                ItemComponentType.Holder.NBT -> {
                    val compound = nbt.getOrPut(id, CompoundShadowTag::create)
                    val holder = ItemComponentHolder.NBT(compound)
                    val cast = type as ItemComponentType.Valued<T, ItemComponentHolder.NBT>
                    cast.write(holder, value)
                }

                ItemComponentType.Holder.ITEM -> {
                    val holder = ItemComponentHolder.Item(item)
                    val cast = type as ItemComponentType.Valued<T, ItemComponentHolder.Item>
                    cast.write(holder, value)
                }

                ItemComponentType.Holder.COMPLEX -> {
                    val compound = nbt.getOrPut(id, CompoundShadowTag::create)
                    val holder = ItemComponentHolder.Complex(item, compound)
                    val cast = type as ItemComponentType.Valued<T, ItemComponentHolder.Complex>
                    cast.write(holder, value)
                }
            }
        }

        override fun <S : ItemComponentHolder> add(type: ItemComponentType.NonValued<S>) {
            val id = type.id
            val source = type.holder
            when (source) {
                // 涉及的组件: 见 has()

                ItemComponentType.Holder.NBT -> {
                    nbt.put(id, CompoundShadowTag.create())
                }

                ItemComponentType.Holder.ITEM -> {
                    val holder = ItemComponentHolder.Item(item)
                    val cast = type as ItemComponentType.NonValued<ItemComponentHolder.Item>
                    cast.write(holder, true)
                }

                ItemComponentType.Holder.COMPLEX -> {
                    val compound = nbt.getOrPut(id, CompoundShadowTag::create)
                    val holder = ItemComponentHolder.Complex(item, compound)
                    val cast = type as ItemComponentType.NonValued<ItemComponentHolder.Complex>
                    cast.write(holder, true)
                }
            }
        }

        override fun remove(type: ItemComponentType) {
            val id = type.id
            val source = type.holder
            when (type) {
                is ItemComponentType.Valued<*, *> -> {
                    when (source) {
                        ItemComponentType.Holder.NBT -> {
                            nbt.remove(id)
                        }

                        ItemComponentType.Holder.ITEM -> {
                            val holder = ItemComponentHolder.Item(item)
                            val cast = type as ItemComponentType.Valued<*, ItemComponentHolder.Item>
                            cast.remove(holder)
                        }

                        ItemComponentType.Holder.COMPLEX -> {
                            // 这个 NBT 标签实际上没必要 getOrPut, 因为反正
                            // 这个标签最后要被移除. 只不过因为 Complex 要求非空.
                            // val compound = nbt.getOrPut(id, CompoundShadowTag::create)

                            val compound = EMPTY_COMPOUND
                            val holder = ItemComponentHolder.Complex(item, compound)
                            val cast = type as ItemComponentType.Valued<*, ItemComponentHolder.Complex>
                            cast.remove(holder)
                            nbt.remove(id)
                        }
                    }
                }

                is ItemComponentType.NonValued<*> -> {
                    when (source) {
                        ItemComponentType.Holder.NBT -> {
                            nbt.remove(id)
                        }

                        ItemComponentType.Holder.ITEM -> {
                            val holder = ItemComponentHolder.Item(item)
                            val cast = type as ItemComponentType.NonValued<ItemComponentHolder.Item>
                            cast.remove(holder)
                        }

                        ItemComponentType.Holder.COMPLEX -> {
                            // val compound = nbt.getOrPut(id, CompoundShadowTag::create)

                            val compound = EMPTY_COMPOUND
                            val holder = ItemComponentHolder.Complex(item, compound)
                            val cast = type as ItemComponentType.NonValued<ItemComponentHolder.Complex>
                            cast.remove(holder)
                            nbt.remove(id)
                        }
                    }
                }
            }
        }

        override fun keySet(): Set<ItemComponentType> {
            return nbt.keySet().mapNotNull { ItemComponentRegistry.TYPES.find(it) }.toSet()
        }

        override fun size(): Int {
            return keySet().size
        }

        override fun toString(): String {
            return nbt.toString()
        }

        private companion object {
            val EMPTY_COMPOUND: CompoundShadowTag = CompoundShadowTag.create()
        }
    }
}

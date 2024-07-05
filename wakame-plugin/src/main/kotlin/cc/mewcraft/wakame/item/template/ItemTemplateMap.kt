package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

// 开发日记: 2024/6/24 小米
// 这个对象要作为 NekoItem 的成员

// 开发日记: 2024/6/25 小米
// 回顾一下总流程, 首先需要经过 configurate 反序列化得到 Template 对象.
// 然后物品生成的时候, 会调用 Template 对象来生成对应的物品组件.
//
// 以前的逻辑:
//   无论一个物品组件(的模板)是否有在配置文件中指定, 它都会被反序列化.
//   这句话的意思是:
//   如果配置文件未指定物品组件的模板,
//   则反序列化会提供一个默认的模板对象,
//   这个默认对象一般是空的, 也就是不生成内容;
//   如果指定了, 则会提供一个具体的模板对象.
//
//   配置文件 到 NekoItem 阶段:
//     用所有的已知 id 为索引, 反序列化每个模板,
//     例如已知 "elements", 则直接 get<SchemaElements>("elements"),
//     这行代码会 hardcode 在构建 NekoItem 实例的代码中.
//     对于每个已知的物品组件, 都会有这样一行代码.
//     这样序列化出来的 NekoItem 是拥有每个物品组件的模板的,
//     只不过有的是默认的空模板, 有的是具体的模板.
//   NekoItem 到 NekoStack 阶段
//     遍历所有储存在 NekoItem 的模板
//       如果模板为空模板, 则不生成;
//       如果模板为具体模板, 则生成
//
// 优化的逻辑:
//   一个物品组件(的类型)如果没有在配置文件中指定,
//   则这个物品组件的模板在配置文件反序列化阶段就会被判定为 null,
//   最终这个物品组件就不会被添加到 NekoItem 的实例中.
//   也就是说, NekoItem 中存在的物品组件模板都是配置文件明确指定的.
//   它们都应该被添加到最终的 NekoStack 上 (除非上下文禁止了某个组件的生成).

/**
 * 代表多个 [ItemTemplate], 包含了一个物品的所有物品组件的模板.
 */
interface ItemTemplateMap : Examinable {

    fun <T> get(type: ItemTemplateType<T>): ItemTemplate<T>?

    fun <T> has(type: ItemTemplateType<T>): Boolean

    // 开发日记 2024/6/26
    // 模板的应用讲究顺序, 而顺序是由储存在模板内部的数据结构决定的.
    // 开发日记 2024/7/4
    // 模板应用到物品的逻辑已经在 NekoItemRealizer 实现
    // /**
    //  * 将模板全部应用到物品上.
    //  */
    // fun applyTo(item: ItemStack)

    companion object {
        /**
         * 返回一个空的 [ItemTemplateMap]. 应用该对象到物品上不会产生任何效果.
         */
        fun empty(): ItemTemplateMap {
            return EmptyMap
        }

        /**
         * 构建一个 [ItemTemplateMap].
         */
        fun build(block: Builder.() -> Unit): ItemTemplateMap {
            return BuilderImpl().apply(block).build()
        }
    }

    /**
     * [ItemTemplate] 的 builder, 用于构建 [ItemTemplateMap].
     */
    interface Builder : Examinable {
        /**
         * 添加一个 [ItemTemplate]. 已存在的 [type] 会被覆盖.
         */
        fun <T> put(type: ItemTemplateType<T>, template: ItemTemplate<T>)

        /**
         * 构建.
         */
        fun build(): ItemTemplateMap
    }

    private object EmptyMap : ItemTemplateMap {
        override fun <T> get(type: ItemTemplateType<T>): ItemTemplate<T>? = null
        override fun <T> has(type: ItemTemplateType<T>): Boolean = false
        override fun toString(): String = toSimpleString()
    }

    private class MapImpl(
        map: LinkedHashMap<ItemTemplateType<*>, ItemTemplate<*>>,
    ) : ItemTemplateMap {
        private val map: LinkedHashMap<ItemTemplateType<*>, ItemTemplate<*>> = LinkedHashMap(map)

        override fun <T> get(type: ItemTemplateType<T>): ItemTemplate<T>? {
            return map[type] as ItemTemplate<T>?
        }

        override fun <T> has(type: ItemTemplateType<T>): Boolean {
            return map.containsKey(type)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map)
        )

        override fun toString(): String {
            return toSimpleString()
        }
    }

    private class BuilderImpl : Builder {
        private val map: LinkedHashMap<ItemTemplateType<*>, ItemTemplate<*>> = LinkedHashMap()

        override fun <T> put(type: ItemTemplateType<T>, template: ItemTemplate<T>) {
            map[type] = template
        }

        override fun build(): ItemTemplateMap {
            return MapImpl(map)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map)
        )

        override fun toString(): String {
            return toSimpleString()
        }
    }
}
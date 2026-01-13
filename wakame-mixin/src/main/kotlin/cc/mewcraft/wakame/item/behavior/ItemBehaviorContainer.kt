package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.item.behavior.ItemBehaviorContainer.Builder
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.lang.reflect.Type
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * 代表一个容器, 存放关于 *物品类型* 的行为逻辑.
 */
sealed interface ItemBehaviorContainer : Iterable<ItemBehavior>, Examinable {

    /**
     * [ItemBehaviorContainer] 的构造函数.
     */
    companion object {

        @JvmField
        val EMPTY: ItemBehaviorContainer = EmptyItemBehaviorContainer

        /**
         * 获取一个 [SimpleSerializer] 用于序列化 [ItemBehaviorContainer].
         */
        fun makeDirectSerializer(): SimpleSerializer<ItemBehaviorContainer> {
            return SimpleItemBehaviorContainer.Serializer
        }

        /**
         * 构建一个 [ItemBehaviorContainer].
         */
        fun build(block: Builder.() -> Unit): ItemBehaviorContainer {
            return builder().apply(block).build()
        }

        /**
         * 获取一个 [Builder] 用来创建 [ItemBehaviorContainer].
         */
        fun builder(): Builder {
            return SimpleItemBehaviorContainer()
        }
    }

    /**
     * 检查该容器是否有指定的 [ItemBehavior].
     * 子类不会被视作相同类型.
     */
    fun hasExact(type: ItemBehavior): Boolean

    /**
     * 检查该容器是否有指定的 [ItemBehavior].
     * 子类将会被视作相同类型.
     */
    fun has(type: KClass<out ItemBehavior>): Boolean

    /**
     * 返回该容器里第一个类型匹配的 [ItemBehavior].
     * 子类将会被视作相同类型.
     */
    operator fun <T : ItemBehavior> get(type: KClass<T>): T?

    /**
     * 返回该容器中包含的 [ItemBehavior] 数量.
     */
    fun size(): Int

    /**
     * 该容器是否为空.
     */
    fun isEmpty(): Boolean

    /**
     * [ItemBehaviorContainer] 的生成器, 用于构建一个 [ItemBehaviorContainer].
     */
    sealed interface Builder {

        /**
         * 添加一个 [ItemBehavior]. 已存在的 [behavior] 会被覆盖.
         */
        fun add(behavior: ItemBehavior)

        /**
         * 添加一个 [ItemBehavior]. 已存在的 [behavior] 会被覆盖.
         */
        operator fun plusAssign(behavior: ItemBehavior) = add(behavior)

        /**
         * 构建.
         */
        fun build(): ItemBehaviorContainer
    }
}

// ------------
// 内部实现
// ------------

private data object EmptyItemBehaviorContainer : ItemBehaviorContainer {
    private val empty: List<ItemBehavior> = emptyList()
    override fun hasExact(type: ItemBehavior): Boolean = false
    override fun has(type: KClass<out ItemBehavior>): Boolean = false
    override fun <T : ItemBehavior> get(type: KClass<T>): T? = null
    override fun size(): Int = 0
    override fun isEmpty(): Boolean = true

    override fun iterator(): Iterator<ItemBehavior> = empty.iterator()
    override fun toString(): String = toSimpleString()
}

// 该 class 同时实现了 ItemBehaviorContainer 和 ItemBehaviorContainer.Builder
private class SimpleItemBehaviorContainer(
    private val behaviorMap: ReferenceArraySet<ItemBehavior> = ReferenceArraySet(),
) : ItemBehaviorContainer, Builder {
    override fun hasExact(type: ItemBehavior): Boolean {
        return behaviorMap.contains(type)
    }

    override fun has(type: KClass<out ItemBehavior>): Boolean {
        return behaviorMap.any { type.isInstance(it) }
    }

    override fun <T : ItemBehavior> get(type: KClass<T>): T? {
        return behaviorMap.firstOrNull { type.isInstance(it) } as T?
    }

    override fun size(): Int {
        return behaviorMap.size
    }

    override fun isEmpty(): Boolean {
        return behaviorMap.isEmpty()
    }

    override fun add(behavior: ItemBehavior) {
        behaviorMap.add(behavior)
    }

    override fun build(): ItemBehaviorContainer {
        return if (behaviorMap.isEmpty()) ItemBehaviorContainer.EMPTY else this
    }

    override fun iterator(): Iterator<ItemBehavior> {
        return behaviorMap.iterator()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("behaviorMap", behaviorMap)
    )

    override fun toString(): String = toSimpleString()

    object Serializer : SimpleSerializer<ItemBehaviorContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemBehaviorContainer {
            val builder = ItemBehaviorContainer.builder()
            for ((rawNodeKey, _) in node.childrenMap()) {
                // 实现上只要 Node 存在那么 ItemBehavior 就存在
                val nodeKey = rawNodeKey.toString()
                val dataValue = BuiltInRegistries.ITEM_BEHAVIOR[nodeKey] ?: continue
                builder += dataValue
            }
            return builder.build()
        }

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemBehaviorContainer? {
            return ItemBehaviorContainer.EMPTY
        }
    }
}

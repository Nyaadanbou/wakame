package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContainer.Builder
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.Collections.emptyList
import java.util.stream.Stream

/**
 * 代表一个容器, 存放关于 *物品类型* 的行为逻辑.
 */
interface ItemBehaviorContainer : Iterable<ItemBehavior>, Examinable {

    /**
     * [ItemBehaviorContainer] 的构造函数.
     */
    companion object {

        fun makeSerializer(): TypeSerializer<ItemBehaviorContainer> {
            return ItemBehaviorContainerImpl.Serializer
        }

        /**
         * 获取一个空的 [ItemBehaviorContainer].
         */
        fun empty(): ItemBehaviorContainer {
            return EmptyItemBehaviorContainer
        }

        /**
         * 构建一个 [ItemBehaviorContainer].
         */
        fun build(block: Builder.() -> Unit): ItemBehaviorContainer {
            return builder().apply(block).build()
        }

        /**
         * 获取一个 [Builder].
         */
        fun builder(): Builder {
            return ItemBehaviorContainerImpl()
        }
    }

    /**
     * 检查该容器是否有指定的 [ItemBehavior].
     */
    infix fun has(type: ItemBehavior): Boolean

    /**
     * 检查该容器是否有指定的 [ItemBehavior].
     */
    operator fun contains(type: ItemBehavior): Boolean = has(type)

    /**
     * [ItemBehaviorContainer] 的生成器, 用于构建一个 [ItemBehaviorContainer].
     */
    interface Builder {

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

private data object EmptyItemBehaviorContainer : ItemBehaviorContainer {
    private val empty: List<ItemBehavior> = emptyList<ItemBehavior>()
    override fun has(type: ItemBehavior): Boolean = false
    override fun iterator(): Iterator<ItemBehavior> = empty.iterator()
    override fun toString(): String = toSimpleString()
}

// 该 class 同时实现了 ItemBehaviorContainer 和 ItemBehaviorContainer.Builder
private class ItemBehaviorContainerImpl(
    private val behaviors: ReferenceArraySet<ItemBehavior> = ReferenceArraySet(),
) : ItemBehaviorContainer, Builder {
    override fun has(type: ItemBehavior): Boolean {
        return behaviors.contains(type)
    }

    override fun add(behavior: ItemBehavior) {
        behaviors.add(behavior)
    }

    override fun build(): ItemBehaviorContainer {
        return if (behaviors.isEmpty()) EmptyItemBehaviorContainer else this
    }

    override fun iterator(): Iterator<ItemBehavior> {
        return behaviors.iterator()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("data", behaviors)
    )

    override fun toString(): String = toSimpleString()

    object Serializer : TypeSerializer<ItemBehaviorContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemBehaviorContainer {
            val builder = ItemBehaviorContainer.builder()
            for ((rawNodeKey, _) in node.childrenMap()) {
                // 实现上只要 Node 存在那么 ItemBehavior 就存在
                val nodeKey = rawNodeKey.toString()
                val dataValue = KoishRegistries2.ITEM_BEHAVIOR[nodeKey] ?: run {
                    LOGGER.error("Unknown item behavior: '$nodeKey'. Skipped.")
                    continue
                }
                builder += dataValue
            }
            return builder.build()
        }
    }
}

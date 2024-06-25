package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

// 开发日记:
// wakame 物品组件其实是个大杂烩,
// 不仅包括了 wakame 自己添加的物品组件,
// 还包括了仅仅是封装了游戏原版物品组件的,
// 甚至还包括了 wakame + 原版的混合组件.

/**
 * 代表一个`物品组件`([ItemComponentType])的`模板`, 可以看成是`物品组件`在配置文件中的抽象.
 *
 * `物品组件`的`模板`专门用来多样化生成`物品组件`的`数据`.
 *
 * @param T 组件快照的类型
 */
interface ItemTemplate<T> {

    // FIXME ItemComponentTemplate 也需要区分 Valued/NonValued 吗?

    /**
     * 生成一个该组件的快照.
     */
    fun generate(context: GenerationContext): GenerationResult<T>

    /**
     * 模板的序列化接口.
     */
    interface Serializer<T : ItemTemplate<*>> : TypeSerializer<T> {
        override fun deserialize(type: Type, node: ConfigurationNode): T

        override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Nothing {
            throw UnsupportedOperationException()
        }

        /**
         * 如果这个物品组件的模板在配置文件中是可选的 (或者说想要其可以省略不写),
         * 那么该函数必须返回一个 **non-null** 的值作为默认的组件模板.
         *
         * 默认实现返回 `null`, 意为该组件模板必须出现在配置文件里.
         */
        override fun emptyValue(specificType: Type?, options: ConfigurationOptions?): T? {
            return null
        }
    }
}
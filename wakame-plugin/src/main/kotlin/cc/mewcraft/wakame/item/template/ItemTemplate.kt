package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

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
 * @param T 组件的数据类型, 也就是必须与对应的 [ItemComponentType] 的泛型一致
 */
interface ItemTemplate<T> : Examinable {

    /**
     * 该模板对应的物品组件.
     */
    val componentType: ItemComponentType<T>

    /**
     * 生成一个该模板的实例.
     *
     * 返回值的要求见 [GenerationResult].
     */
    fun generate(context: GenerationContext): GenerationResult<T>
}
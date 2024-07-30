package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.Element as ElementType

/**
 * 用于构成 [AttributeComponentGroup] 的一部分.
 *
 * 这些接口仅用于理清类的继承结构, 你不应该直接实现这些接口!
 */
sealed interface AttributeComponent {
    /**
     * 运算模式.
     */
    interface Op : AttributeComponent {
        val operation: AttributeModifier.Operation
    }

    /**
     * 元素.
     */
    interface Element : AttributeComponent {
        val element: ElementType
    }

    /**
     * 固定值.
     */
    interface Fixed<T> : AttributeComponent {
        val value: T
    }

    /**
     * 范围值.
     */
    interface Ranged<T> : AttributeComponent {
        val lower: T
        val upper: T
    }
}

/**
 * 这是一个高度抽象的接口, 它代表:
 * - 一个玩家视角下的属性 (例如物品上面的攻击力)
 * - 一个由配置文件定义的属性 (例如随机池里的属性)
 *
 * 而本质上, 它是多个 [AttributeComponent] 的组合.
 *
 * 为什么需要这个接口? 原因有以下几点:
 *
 * ## 玩家看到的"属性"不一定只是一个
 *
 * 首先要明确的是, 技术上所说的属性就是 [Attribute] 以及对应的
 * [AttributeInstance]. 但玩家看到的单个“属性”并不一定只有单个
 * [Attribute] 和 [AttributeInstance].
 *
 * 例如, 玩家看到的物品“攻击力”属性, 实际上是由两个 [Attribute] 组成的.
 * 一个是 [ElementAttributes.MIN_ATTACK_DAMAGE], 另一个是
 * [ElementAttributes.MAX_ATTACK_DAMAGE].
 *
 * 因此我们需要这么一个类, 将这两个 [Attribute] 组合成一个玩家眼中的“属性”.
 *
 * ## 属性本身不仅有数值, 还有其他部分
 *
 * 技术上, 属性只是一个类型和数值的组合, 系统本身对于其他部分是毫无感知的.
 * 但从玩家的角度来看, 一个属性它有的时候具有元素类型, 意为这个属性的效果
 * 会随着元素的不同而不同.
 *
 * 我们需要这么一个接口, 使得系统本身也能感知到元素, 从而实现更复杂的功能.
 *
 * ## 方便其他系统与属性系统进行交互
 *
 * 属性系统除了要进行本身的数值计算, 还需要提供一种方式,
 * 使得其他模块能够方便的与属性系统进行交互.
 * 例如:
 * - 允许用户使用配置文件为一个自定义物品添加属性, 并且这个属性还支持在物品生成时产生随机的数值.
 * - 允许其他系统可以快速的生成一个属性的文字描述, 并且这个描述还要跟玩家视角下的属性在概念上一致.
 *
 * 这些功能都需要一个统一的接口来实现, 以增加代码的可读性.
 */
interface AttributeComponentGroup : AttributeComponent.Op /* 所有属性都有 Op */

/**
 * 组件: [AttributeComponent.Op], [AttributeComponent.Fixed]
 *
 * 这种属性只有一个固定的数值, 典型代表: 移速.
 */
interface AttributeComponentGroupS<T> : AttributeComponentGroup, AttributeComponent.Fixed<T> {
    override val value: T
}

/**
 * 组件: [AttributeComponent.Op], [AttributeComponent.Fixed]
 *
 * 这种属性有一个最小值和最大值, 典型代表: 暂无.
 */
interface AttributeComponentGroupR<T> : AttributeComponentGroup, AttributeComponent.Ranged<T> {
    override val lower: T
    override val upper: T
}

/**
 * 组件: [AttributeComponent.Op], [AttributeComponent.Fixed], [AttributeComponent.Element]
 *
 * 这种属性只有一个固定的数值, 并且有一个元素类型, 典型代表: 所有元素伤害加成.
 */
interface AttributeComponentGroupSE<T> : AttributeComponentGroup, AttributeComponent.Fixed<T>, AttributeComponent.Element {
    override val value: T
    override val element: Element
}

/**
 * 组件: [AttributeComponent.Op], [AttributeComponent.Ranged], [AttributeComponent.Element]
 *
 * 这种属性有一个最小值和最大值, 并且有一个元素类型, 典型代表: 基于元素的浮动攻击力.
 */
interface AttributeComponentGroupRE<T> : AttributeComponentGroup, AttributeComponent.Ranged<T>, AttributeComponent.Element {
    override val lower: T
    override val upper: T
    override val element: ElementType
}

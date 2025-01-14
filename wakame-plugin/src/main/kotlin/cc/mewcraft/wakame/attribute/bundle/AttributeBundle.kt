package cc.mewcraft.wakame.attribute.bundle

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.entry.RegistryEntry

/**
 * 代表 [AttributeBundle] 的一部分特性, 故命名为 "...Trait".
 */
sealed interface AttributeBundleTrait {
    /**
     * 运算模式.
     */
    interface Operation : AttributeBundleTrait {
        val operation: AttributeModifier.Operation
    }

    /**
     * 元素.
     */
    interface Element : AttributeBundleTrait {
        val element: RegistryEntry<ElementType>
    }

    /**
     * 单一数值.
     */
    interface Scalar<T> : AttributeBundleTrait {
        val value: T
    }

    /**
     * 区间数值.
     */
    interface Ranged<T> : AttributeBundleTrait {
        val lower: T
        val upper: T
    }
}

/**
 * 这是一个高度抽象的接口, 可以称之为 “属性块”, 它代表:
 * - 一个玩家视角下的属性 (例如物品上面的攻击力)
 * - 一个由配置文件定义的属性 (例如随机池里的属性)
 *
 * 而本质上, 它是多个 [AttributeBundleTrait] 的组合.
 *
 * 为什么需要这个接口? 原因有以下几点:
 *
 * ## 玩家看到的"属性"不一定只是一个
 *
 * 首先要明确的是, 技术上所说的属性就是 [cc.mewcraft.wakame.attribute.Attribute] 以及对应的
 * [cc.mewcraft.wakame.attribute.AttributeInstance]. 但玩家看到的单个“属性”并不一定只有单个
 * [cc.mewcraft.wakame.attribute.Attribute] 和 [cc.mewcraft.wakame.attribute.AttributeInstance].
 *
 * 例如, 玩家看到的物品“攻击力”属性, 实际上是由两个 [cc.mewcraft.wakame.attribute.Attribute] 组成的.
 * 一个是 [cc.mewcraft.wakame.attribute.Attributes.MIN_ATTACK_DAMAGE], 另一个是
 * [cc.mewcraft.wakame.attribute.Attributes.MAX_ATTACK_DAMAGE].
 *
 * 因此我们需要这么一个类, 将这两个 [cc.mewcraft.wakame.attribute.Attribute] 组合成一个玩家眼中的“属性”.
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
sealed interface AttributeBundle : AttributeBundleTrait.Operation /* 所有属性都有 Operation */ {
    /**
     * 该 [AttributeBundle] 的唯一标识.
     */
    val id: String
}

/**
 * 拥有组件:
 * - [AttributeBundleTrait.Operation]
 * - [AttributeBundleTrait.Scalar]
 *
 * 这种属性只有一个固定的数值. 典型代表: 移速.
 */
interface AttributeBundleS<T> : AttributeBundle, AttributeBundleTrait.Scalar<T> {
    override val value: T
}

/**
 * 拥有组件:
 * - [AttributeBundleTrait.Operation]
 * - [AttributeBundleTrait.Scalar]
 *
 * 这种属性有一个最小值和最大值. 典型代表: 暂无.
 */
interface AttributeBundleR<T> : AttributeBundle, AttributeBundleTrait.Ranged<T> {
    override val lower: T
    override val upper: T
}

/**
 * 拥有组件:
 * - [AttributeBundleTrait.Operation]
 * - [AttributeBundleTrait.Scalar]
 * - [AttributeBundleTrait.Element]
 *
 * 这种属性只有一个固定的数值, 并且有一个元素类型. 典型代表: 所有元素伤害加成.
 */
interface AttributeBundleSE<T> : AttributeBundle, AttributeBundleTrait.Scalar<T>, AttributeBundleTrait.Element {
    override val value: T
    override val element: RegistryEntry<ElementType>
}

/**
 * 拥有组件:
 * - [AttributeBundleTrait.Operation],
 * - [AttributeBundleTrait.Ranged],
 * - [AttributeBundleTrait.Element]
 *
 * 这种属性有一个最小值和最大值, 并且有一个元素类型. 典型代表: 基于元素的浮动攻击力.
 */
interface AttributeBundleRE<T> : AttributeBundle, AttributeBundleTrait.Ranged<T>, AttributeBundleTrait.Element {
    override val lower: T
    override val upper: T
    override val element: RegistryEntry<ElementType>
}

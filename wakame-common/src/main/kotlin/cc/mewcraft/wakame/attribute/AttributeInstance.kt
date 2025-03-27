package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key

/**
 * *属性实例* [AttributeInstance] 本质上是一个用于数值计算的容器, 里面包含了一个
 * [Attribute] 和多个与之关联的 [AttributeModifier]. 每一个 [AttributeModifier]
 * 在概念上都是对 [Attribute] 的修饰, 会影响 [AttributeInstance] 的数值计算结果.
 *
 * **Caution:** This should be directly linked to a certain living entity,
 * e.g. a player, a zombie, etc. Any changes on this instance
 * should reflect on the linked entity in real time.
 */
interface AttributeInstance : AttributeInstanceSnapshotable {
    /**
     * The [Attribute] in this [AttributeInstance].
     *
     * The object serves as a "prototype", where we might take the [Attribute.defaultValue]
     * as the return value of [getBaseValue], or we just read the `baseValue` stored in
     * the [AttributeInstance] object.
     */
    val attribute: Attribute

    /**
     * 获取经过所有 [AttributeModifier] 修饰后的最终数值.
     */
    fun getValue(): Double

    /**
     * 获取该属性实例的基值.
     */
    fun getBaseValue(): Double

    /**
     * 设置该属性实例的基值.
     */
    fun setBaseValue(baseValue: Double)

    /**
     * 获取指定 [id] 的 [AttributeModifier].
     */
    fun getModifier(id: Key): AttributeModifier?

    /**
     * 返回一个包含所有 [AttributeModifier] 的集合. 返回的集合不可变.
     */
    fun getModifiers(): Set<AttributeModifier>

    /**
     * 判断是否存在指定的 [AttributeModifier].
     */
    fun hasModifier(modifier: AttributeModifier): Boolean

    /**
     * 添加一个 [AttributeModifier] 到该属性实例.
     */
    fun addModifier(modifier: AttributeModifier)

    /**
     * 添加一个短暂的 [AttributeModifier] 到该属性实例.
     */
    fun addTransientModifier(modifier: AttributeModifier)

    /**
     * 移除指定的 [AttributeModifier].
     */
    fun removeModifier(modifier: AttributeModifier)

    /**
     * 移除指定 [id] 的 [AttributeModifier].
     */
    fun removeModifier(id: Key)

    /**
     * 移除所有 [AttributeModifier].
     */
    fun removeModifiers()

    /**
     * 将本实例的所有状态替换为 [other] 的状态.
     */
    fun replace(other: AttributeInstance)

    /*
       开发日记 2024/10/17
       此方法的存在是为了解决一个问题: 原本的 replace 方法会导致实例的基值与 modifier 一起被替换,
       但是应用它的地方都是在需要保留 modifier 的情况下才能运作 (如 AttributeSupplier 的应用).
       因此, 我们需要一个来替换实例的基值但保留 modifier 的方法.
    */
    /**
     * 将本实例的基值替换为 [other] 的基值.
     */
    fun replaceBaseValue(other: AttributeInstance)
}

/**
 * 虚拟的属性实例.
 * 目前用于实体本身没有属性, 但是需要计算属性的场景.
 * 例如: 箭矢提供的额外伤害, 三叉戟本身的伤害数值.
 *
 * 该实例不可变, 并且不会对世界状态产生任何副作用.
 */
interface ImaginaryAttributeInstance : AttributeInstanceSnapshotable {
    /**
     * 该属性实例的 [Attribute].
     */
    val attribute: Attribute

    /**
     * 获取经过所有 [AttributeModifier] 修饰后的最终数值.
     */
    fun getValue(): Double

    /**
     * 获取该属性实例的基值.
     */
    fun getBaseValue(): Double

    /**
     * 获取指定 [id] 的 [AttributeModifier].
     */
    fun getModifier(id: Key): AttributeModifier?

    /**
     * 返回一个包含所有 [AttributeModifier] 的集合. 返回的集合不可变.
     */
    fun getModifiers(): Set<AttributeModifier>

    /**
     * 判断是否存在指定的 [AttributeModifier].
     */
    fun hasModifier(modifier: AttributeModifier): Boolean
}

/**
 * 代表一个属性实例的快照 (支持读/写), 用于临时的数值储存和计算.
 */
interface AttributeInstanceSnapshot : AttributeInstanceSnapshotable {
    /**
     * 该属性实例的 [Attribute].
     */
    val attribute: Attribute

    /**
     * 获取经过所有 [AttributeModifier] 修饰后的最终数值.
     */
    fun getValue(): Double

    /**
     * 获取该属性实例的基值.
     */
    fun getBaseValue(): Double

    /**
     * 设置该属性实例的基值.
     */
    fun setBaseValue(baseValue: Double)

    /**
     * 获取指定 [id] 的 [AttributeModifier].
     */
    fun getModifier(id: Key): AttributeModifier?

    /**
     * 返回一个包含所有 [AttributeModifier] 的集合. 返回的集合不可变.
     */
    fun getModifiers(): Set<AttributeModifier>

    /**
     * 判断是否存在指定的 [AttributeModifier].
     */
    fun hasModifier(modifier: AttributeModifier): Boolean

    /**
     * 添加一个 [AttributeModifier] 到该属性实例.
     */
    fun addModifier(modifier: AttributeModifier)

    /**
     * 移除指定的 [AttributeModifier].
     */
    fun removeModifier(modifier: AttributeModifier)

    /**
     * 移除指定 [id] 的 [AttributeModifier].
     */
    fun removeModifier(id: Key)

    /**
     * 移除所有 [AttributeModifier].
     */
    fun removeModifiers()

    /**
     * 将本实例转换为一个 [ImaginaryAttributeInstance].
     */
    fun toImaginary(): ImaginaryAttributeInstance
}

/**
 * 代表一个可以产生 [AttributeInstanceSnapshot] 的对象.
 */
interface AttributeInstanceSnapshotable {
    /**
     * 根据当前状态创建一个 [AttributeInstanceSnapshot].
     */
    fun getSnapshot(): AttributeInstanceSnapshot
}
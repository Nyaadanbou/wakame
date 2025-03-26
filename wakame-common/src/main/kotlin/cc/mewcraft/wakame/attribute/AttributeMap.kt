package cc.mewcraft.wakame.attribute

import com.google.common.collect.Multimap
import net.kyori.adventure.key.Key

/**
 * 代表一个从 [属性类型][Attribute] 到 [属性实例][AttributeInstance] 的映射.
 */
interface AttributeMapLike {
    /**
     * 获取所有的属性, 也就是 [hasAttribute] 返回 `true` 的属性.
     */
    fun getAttributes(): Set<Attribute>

    /**
     * 检查是否存在 [attribute].
     */
    fun hasAttribute(attribute: Attribute): Boolean

    /**
     * 检查是否存在 [attribute] 的指定 [id] 的修饰器.
     */
    fun hasModifier(attribute: Attribute, id: Key): Boolean

    /**
     * 获取 [attribute] 的值.
     */
    fun getValue(attribute: Attribute): Double

    /**
     * 获取 [attribute] 的基值.
     */
    fun getBaseValue(attribute: Attribute): Double

    /**
     * 获取 [attribute] 的指定 [id] 的修饰器的值.
     */
    fun getModifierValue(attribute: Attribute, id: Key): Double
}

/**
 * 代表一个 [AttributeMapLike] 的快照, 支持读/写, 用于临时的数值储存和计算.
 */
interface AttributeMapSnapshot : AttributeMapLike, Iterable<Map.Entry<Attribute, AttributeInstanceSnapshot>> {
    /**
     * 获取指定 [attribute] 的实例快照.
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): AttributeInstanceSnapshot?

    /**
     * 添加临时的 [AttributeModifier].
     */
    fun addTransientModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)

    /**
     * 移除非默认的 [AttributeModifier].
     */
    fun removeModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)
}

/**
 * 代表一个可以创建 [AttributeMapSnapshot] 的对象.
 */
interface AttributeMapSnapshotable {
    /**
     * 根据当前状态创建一个 [AttributeMapSnapshot].
     */
    fun getSnapshot(): AttributeMapSnapshot
}

/**
 * 代表一个标准的 [AttributeMapLike], 支持读/写.
 *
 * 该对象在实现上必须与一个主体绑定, 例如玩家, 怪物等.
 * **任何对该对象的修改都应该实时反应到绑定的主体上!**
 */
interface AttributeMap : AttributeMapLike, AttributeMapSnapshotable, Iterable<Map.Entry<Attribute, AttributeInstance>> {
    /**
     * 获取指定 [attribute] 的 [AttributeInstance].
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): AttributeInstance?

    /**
     * 添加临时的 [AttributeModifier].
     */
    fun addTransientModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)

    /**
     * 移除非默认的 [AttributeModifier].
     */
    fun removeModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)

    /**
     * 注册指定 [attribute]. 这将覆盖任何已存在的 [AttributeInstance].
     */
    fun registerInstance(attribute: Attribute)
}

/**
 * 代表一个虚拟的 [AttributeMapLike], 不支持写入.
 *
 * @see ImaginaryAttributeInstance
 */
interface ImaginaryAttributeMap : AttributeMapLike, AttributeMapSnapshotable {
    /**
     * 获取指定 [attribute] 的 [ImaginaryAttributeInstance].
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): ImaginaryAttributeInstance?
}
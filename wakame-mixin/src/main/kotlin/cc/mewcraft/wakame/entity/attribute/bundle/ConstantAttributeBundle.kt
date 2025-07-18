package cc.mewcraft.wakame.entity.attribute.bundle

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.entity.attribute.AttributeModifierSource
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.registerExact
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 该属性核心的元素种类. 如果该属性核心没有元素, 则返回 `null`.
 */
val ConstantAttributeBundle.element: RegistryEntry<Element>?
    get() = (this as? AttributeBundleTrait.Element)?.element

/**
 * 从配置文件构建一个 [ConstantAttributeBundle].
 *
 * 给定的 [ConfigurationNode] 必须是以下结构之一:
 *
 * ## 对于 ConstantAttributeBundleS
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * value: <double>
 * ```
 *
 * ## 对于 ConstantAttributeBundleSE
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * value: <double>
 * element: <element>
 * ```
 *
 * ## 对于 ConstantAttributeBundleR
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * lower: <double>
 * upper: <double>
 * ```
 *
 * ## 对于 ConstantAttributeBundleRE
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * lower: <double>
 * upper: <double>
 * element: <element>
 * ```
 */
fun ConstantAttributeBundle(
    id: String, node: ConfigurationNode,
): ConstantAttributeBundle {
    return BuiltInRegistries.ATTRIBUTE_FACADE.getOrThrow(id).convertNodeToConstant(node)
}

/**
 * 代表一个数值恒定的 [AttributeBundle].
 */
sealed class ConstantAttributeBundle : AttributeBundle, AttributeModifierSource {

    companion object {
        @JvmField
        val SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
            .registerExact(
                DispatchingSerializer.create<String, ConstantAttributeBundle>(
                    { obj -> obj.id },
                    { key -> BuiltInRegistries.ATTRIBUTE_FACADE.getOrThrow(key).valueType }
                )
            )
            .build()
    }

    val displayName: Component
        get() = BuiltInRegistries.ATTRIBUTE_FACADE.getOrThrow(id).createTooltipName(this)

    val description: List<Component>
        get() = BuiltInRegistries.ATTRIBUTE_FACADE.getOrThrow(id).createTooltipLore(this)
    /**
     * 数值的质量, 通常以正态分布的 Z-score 转换而来.
     *
     * 并不是每个属性块都有数值的质量,
     * 例如用于铭刻的属性块就没有数值质量,
     * 因为它们都已经在配置文件中固定好的.
     *
     * 如果该属性块有多个数值, 例如 [AttributeBundleR],
     * 那么只会储存 [AttributeBundleR.upper]
     * 的数值质量.
     */
    abstract val quality: Quality?

    /**
     * 检查两个 [ConstantAttributeBundle] 是否拥有一样的:
     * - 运算模式
     * - 数值结构
     * - 元素类型 (如果有)
     *
     * 该函数不会检查任何数值的相等性.
     */
    abstract fun similarTo(other: ConstantAttributeBundle): Boolean

    /**
     * 生成该 值属性块 的属性修饰符.
     */
    override fun createAttributeModifiers(modifierId: Key): Map<Attribute, AttributeModifier> {
        return BuiltInRegistries.ATTRIBUTE_FACADE.getOrThrow(id).createAttributeModifiers(modifierId, this)
    }

    /**
     * 属性核心的“数值质量”.
     * [Quality.ordinal] 越小则数值质量越差, 反之越好.
     */
    enum class Quality {
        L3, L2, L1, MU, U1, U2, U3;

        companion object {
            /**
             * 从正态分布的 Z-score 转换为 [Quality].
             */
            fun fromZScore(score: Double): Quality {
                return when {
                    score < -2.5 -> L3
                    score < -1.5 -> L2
                    score < -0.5 -> L1
                    score < 0.5 -> MU
                    score < 1.5 -> U1
                    score < 2.5 -> U2
                    else -> U3
                }
            }
        }
    }
}

private const val SINGLE_VALUE_FIELD = "val"
private const val RANGED_MIN_VALUE_FIELD = "min"
private const val RANGED_MAX_VALUE_FIELD = "max"
private const val ELEMENT_TYPE_FIELD = "elem"
private const val OPERATION_TYPE_FIELD = "op"
private const val QUALITY_FIELD = "qual"

@ConfigSerializable
data class ConstantAttributeBundleS(
    @Setting("type") // 投机取巧了一下, 和 type 共用一个键名
    override val id: String,
    override val operation: Operation,
    override val value: Double,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleS<Double> {

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleS && other.id == id && other.operation == operation
    }
}

@ConfigSerializable
data class ConstantAttributeBundleR(
    @Setting("type")
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleR<Double> {

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleR && other.id == id && other.operation == operation
    }
}

@ConfigSerializable
data class ConstantAttributeBundleSE(
    @Setting("type")
    override val id: String,
    override val operation: Operation,
    override val value: Double,
    override val element: RegistryEntry<Element>,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleSE<Double> {

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleSE && other.id == id && other.operation == operation && other.element == element
    }
}

@ConfigSerializable
data class ConstantAttributeBundleRE(
    @Setting("type")
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: RegistryEntry<Element>,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleRE<Double> {

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleRE && other.id == id && other.operation == operation && other.element == element
    }
}

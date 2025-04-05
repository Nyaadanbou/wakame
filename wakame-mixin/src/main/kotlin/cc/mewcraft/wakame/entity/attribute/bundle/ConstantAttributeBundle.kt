package cc.mewcraft.wakame.entity.attribute.bundle

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.entity.attribute.AttributeModifierSource
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.data.getByteOrNull
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.spongepowered.configurate.ConfigurationNode

/**
 * 该属性核心的元素种类. 如果该属性核心没有元素, 则返回 `null`.
 */
val ConstantAttributeBundle.element: RegistryEntry<Element>?
    get() = (this as? AttributeBundleTrait.Element)?.element

/**
 * 从 NBT 构建一个 [ConstantAttributeBundle].
 *
 * 给定的 [CompoundTag] 必须是以下结构之一:
 *
 * ## 对于 ConstantAttributeBundleS
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('value'): <double>
 * byte('quality'): <quality>
 * ```
 *
 * ## 对于 ConstantAttributeBundleSE
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('value'): <double>
 * byte('element'): <element>
 * byte('quality'): <quality>
 * ```
 *
 * ## 对于 ConstantAttributeBundleR
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('lower'): <double>
 * byte('upper'): <double>
 * byte('quality'): <quality>
 * ```
 *
 * ## 对于 ConstantAttributeBundleRE
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('lower'): <double>
 * byte('upper'): <double>
 * byte('element'): <element>
 * byte('quality'): <quality>
 * ```
 */
fun ConstantAttributeBundle(
    id: String, tag: CompoundTag,
): ConstantAttributeBundle {
    return KoishRegistries2.ATTRIBUTE_FACADE.getOrThrow(id).convertNbtToConstant(tag)
}

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
    return KoishRegistries2.ATTRIBUTE_FACADE.getOrThrow(id).convertNodeToConstant(node)
}

/**
 * 代表一个数值恒定的 [AttributeBundle].
 */
sealed class ConstantAttributeBundle : AttributeBundle, AttributeModifierSource {

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

    val displayName: Component
        get() = KoishRegistries2.ATTRIBUTE_FACADE.getOrThrow(id).createTooltipName(this)
    val description: List<Component>
        get() = KoishRegistries2.ATTRIBUTE_FACADE.getOrThrow(id).createTooltipLore(this)

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
     * 序列化为 NBT 标签. 请注意这并不包含 [id] 的信息.
     */
    abstract fun saveNbt(): CompoundTag

    override fun createAttributeModifiers(modifierId: Key): Map<Attribute, AttributeModifier> {
        return KoishRegistries2.ATTRIBUTE_FACADE.getOrThrow(id).createAttributeModifiers(modifierId, this)
    }
}

private const val SINGLE_VALUE_FIELD = "val"
private const val RANGED_MIN_VALUE_FIELD = "min"
private const val RANGED_MAX_VALUE_FIELD = "max"
private const val ELEMENT_TYPE_FIELD = "elem"
private const val OPERATION_TYPE_FIELD = "op"
private const val QUALITY_FIELD = "qual"

data class ConstantAttributeBundleS(
    override val id: String,
    override val operation: Operation,
    override val value: Double,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleS<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(SINGLE_VALUE_FIELD),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleS && other.id == id && other.operation == operation
    }

    override fun saveNbt(): CompoundTag = CompoundTag().apply {
        writeOperation(operation)
        writeNumber(SINGLE_VALUE_FIELD, value)
        writeQuality(quality)
    }
}

data class ConstantAttributeBundleR(
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleR<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(RANGED_MIN_VALUE_FIELD),
        compound.readNumber(RANGED_MAX_VALUE_FIELD),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleR && other.id == id && other.operation == operation
    }

    override fun saveNbt(): CompoundTag = CompoundTag().apply {
        writeOperation(operation)
        writeNumber(RANGED_MIN_VALUE_FIELD, lower)
        writeNumber(RANGED_MAX_VALUE_FIELD, upper)
        writeQuality(quality)
    }
}

data class ConstantAttributeBundleSE(
    override val id: String,
    override val operation: Operation,
    override val value: Double,
    override val element: RegistryEntry<Element>,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleSE<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(SINGLE_VALUE_FIELD),
        compound.readElement(),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleSE && other.id == id && other.operation == operation && other.element == element
    }

    override fun saveNbt(): CompoundTag = CompoundTag().apply {
        writeOperation(operation)
        writeNumber(SINGLE_VALUE_FIELD, value)
        writeElement(element)
        writeQuality(quality)
    }
}

data class ConstantAttributeBundleRE(
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: RegistryEntry<Element>,
    override val quality: Quality? = null,
) : ConstantAttributeBundle(), AttributeBundleRE<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(RANGED_MIN_VALUE_FIELD),
        compound.readNumber(RANGED_MAX_VALUE_FIELD),
        compound.readElement(),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantAttributeBundle): Boolean {
        return other is ConstantAttributeBundleRE && other.id == id && other.operation == operation && other.element == element
    }

    override fun saveNbt(): CompoundTag = CompoundTag().apply {
        writeOperation(operation)
        writeNumber(RANGED_MIN_VALUE_FIELD, lower)
        writeNumber(RANGED_MAX_VALUE_FIELD, upper)
        writeElement(element)
        writeQuality(quality)
    }
}

private fun CompoundTag.readElement(): RegistryEntry<Element> {
    val id = getString(ELEMENT_TYPE_FIELD)
    return KoishRegistries2.ELEMENT.getEntry(id)
        ?: KoishRegistries2.ELEMENT.getDefaultEntry()
}

private fun CompoundTag.readOperation(): Operation {
    val id = getByte(OPERATION_TYPE_FIELD).toInt()
    return Operation.byId(id) ?: error("No such operation with id: $id")
}

private fun CompoundTag.readNumber(key: String): Double {
    return getDouble(key)
}

private fun CompoundTag.readQuality(): ConstantAttributeBundle.Quality? {
    val id = getByteOrNull(QUALITY_FIELD)?.toInt() ?: return null
    return ConstantAttributeBundle.Quality.entries[id]
}

private fun CompoundTag.writeNumber(key: String, value: Double) {
    putDouble(key, value)
}

private fun CompoundTag.writeElement(element: RegistryEntry<Element>) {
    val id = element.getIdAsString()
    putString(ELEMENT_TYPE_FIELD, id)
}

private fun CompoundTag.writeOperation(operation: Operation) {
    val id = operation.id.toByte()
    putByte(OPERATION_TYPE_FIELD, id)
}

private fun CompoundTag.writeQuality(quality: ConstantAttributeBundle.Quality?) {
    val id = quality?.ordinal?.toByte() ?: return
    putByte(QUALITY_FIELD, id)
}

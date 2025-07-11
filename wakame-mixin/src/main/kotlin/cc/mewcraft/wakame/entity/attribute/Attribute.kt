@file:Suppress("MemberVisibilityCanBePrivate")

package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Identified
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.intellij.lang.annotations.Pattern
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.orElse
import java.util.stream.Stream


const val ATTRIBUTE_ID_PATTERN_STRING = "[a-z0-9_./]+"

val GLOBAL_ATTRIBUTE_CONFIG = ConfigAccess.INSTANCE["attributes"]

/**
 * 代表一种 *属性*. 一般作为静态常量使用.
 *
 * 使用 [AttributeProvider] 来获取实例.
 */
interface Attribute : Identified {

    /**
     * 本属性的唯一标识.
     */
    val id: String

    /**
     * 本属性所属的属性包的唯一标识.
     */
    val bundleId: String

    /**
     * 本属性的默认值.
     */
    val defaultValue: Double

    /**
     * 本属性是否为原版属性.
     */
    val vanilla: Boolean

    /**
     * 使 [value] 落在本属性规定的数值范围之内.
     *
     * @param value 待清理的数值
     * @return 清理好的数值
     */
    fun sanitizeValue(value: Double): Double

}

/**
 * An attribute type with a numerical default value.
 *
 * ## Notes to users of the code
 *
 * By design, you should not create the instance yourself because the objects
 * are generally used as conceptual types. Instead, use the singleton [Attributes]
 * to get the instances. The same also applies to its subtypes.
 *
 * @property bundleId 组合属性的唯一标识
 * @property id 单个属性的唯一标识
 * @property defaultValue 属性的默认数值 (非 [Provider])
 * @property vanilla 属性是否由原版属性实现
 *
 * @see RangedAttribute
 * @see ElementAttribute
 */
open class SimpleAttribute
/**
 * @param defaultValue 属性的默认数值 ([Provider])
 */
protected constructor(
    @Pattern(ATTRIBUTE_ID_PATTERN_STRING)
    final override val id: String,
    @Pattern(ATTRIBUTE_ID_PATTERN_STRING)
    final override val bundleId: String,
    defaultValue: Provider<Double>,
    final override val vanilla: Boolean = false,
) : Examinable, Attribute {

    init {
        BuiltInRegistries.ATTRIBUTE.add(id, this) // 添加到 KoishRegistries
    }

    // 需要注意 (kotlin 委托基础)
    // 当一个 property 将值委托给 provider 时，其返回的是 provider 的值，而不是 provider
    // 也就是说，从 defaultValue 拿到的值它是常量，是不会自动更新的
    // 或者，你也可以简单的理解成因为 double 是常量，所以不会自动更新
    //
    // 所以说，当你想要 AttributeInstance#getBaseValue 的值能自动更新，
    // 你应该直接返回 Attribute#defaultValue 的值，而不是赋值给其他 val 然后返回那个 val
    // 这也意味着，我们也不需要再写一个 defaultValueProvider 的 property
    //
    // 这里说的同样也适用于该文件其他用到 `by` 的地方
    final override val defaultValue: Double by defaultValue

    /**
     * Instantiates the type using the global attribute config as value providers.
     *
     * This constructor is used if the [bundleId] is different from the [id].
     *
     * @param bundleId the bundle id of attribute to which this attribute is related
     */
    internal constructor(
        id: String,
        bundleId: String,
        defaultValue: Double,
        vanilla: Boolean = false,
    ) : this(
        id = id,
        bundleId = bundleId,
        defaultValue = GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(bundleId, "values", "default").orElse(defaultValue),
        vanilla = vanilla
    )

    internal constructor(
        id: String,
        defaultValue: Double,
        vanilla: Boolean = false,
    ) : this(
        id = id,
        bundleId = id,
        defaultValue = defaultValue,
        vanilla = vanilla,
    )

    override fun sanitizeValue(value: Double): Double {
        return value
    }

    override val identifier: Identifier = Key.key(Namespaces.ATTRIBUTE, id)

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("descriptionId", id),
            ExaminableProperty.of("defaultValue", defaultValue),
            ExaminableProperty.of("vanilla", vanilla),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is Attribute)
            return id == other.id
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * An [Attribute] type with bounded values.
 *
 * The [minValue] and [maxValue] put a threshold on the final value of this attribute
 * after all [AttributeModifier]s have been applied.
 */
open class RangedAttribute
/**
 * @param minValue 该属性允许的最小数值（[Provider]）
 * @param maxValue 该属性允许的最大数值（[Provider]）
 */
protected constructor(
    id: String,
    bundleId: String,
    defaultValue: Provider<Double>,
    minValue: Provider<Double>,
    maxValue: Provider<Double>,
    vanilla: Boolean = false,
) : SimpleAttribute(id, bundleId, defaultValue, vanilla) {
    val minValue: Double by minValue
    val maxValue: Double by maxValue

    /**
     * Instantiates the type using the global attribute config as value providers.
     *
     * This constructor is used if the [bundleId] is different from the [id].
     *
     * @param bundleId the bundle id of attribute to which this attribute is related
     */
    internal constructor(
        id: String,
        bundleId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        vanilla: Boolean = false,
    ) : this(
        id = id,
        bundleId = bundleId,
        defaultValue = GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(bundleId, "values", "default").orElse(defaultValue),
        minValue = GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(bundleId, "values", "min").orElse(minValue),
        maxValue = GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(bundleId, "values", "max").orElse(maxValue),
        vanilla = vanilla
    )

    internal constructor(
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        vanilla: Boolean = false,
    ) : this(
        id = descriptionId,
        bundleId = descriptionId,
        defaultValue = defaultValue,
        minValue = minValue,
        maxValue = maxValue,
        vanilla = vanilla
    )

    init {
        if (this.minValue > this.maxValue) {
            throw IllegalArgumentException("Minimum value cannot be higher than maximum value!")
        } else if (this.defaultValue < this.minValue) {
            throw IllegalArgumentException("Default value cannot be lower than minimum value!")
        } else if (this.defaultValue > this.maxValue) {
            throw IllegalArgumentException("Default value cannot be higher than maximum value!")
        }
    }

    override fun sanitizeValue(value: Double): Double {
        return if (value.isNaN()) {
            minValue
        } else {
            value.coerceIn(minValue, maxValue)
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.concat(
            super.examinableProperties(),
            Stream.of(
                ExaminableProperty.of("minValue", minValue),
                ExaminableProperty.of("maxValue", maxValue),
            ),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is RangedAttribute)
            return this@RangedAttribute.id == other.id /* && element == other.element */
        return false
    }

    override fun hashCode(): Int {
        val result = this@RangedAttribute.id.hashCode()
        // result = (31 * result) + element.hashCode()
        return result
    }
}

/**
 * An [Attribute] type with an [Element].
 */
open class ElementAttribute
protected constructor(
    id: String,
    bundleId: String,
    defaultValue: Provider<Double>,
    minValue: Provider<Double>,
    maxValue: Provider<Double>,
    val element: RegistryEntry<Element>,
    vanilla: Boolean = false,
) : RangedAttribute(
    id + ELEMENT_SEPARATOR + element.getIdAsString(),
    bundleId + ELEMENT_SEPARATOR + element.getIdAsString(),
    defaultValue,
    minValue,
    maxValue,
    vanilla,
) {

    companion object Constants {
        /**
         * The separator used to separate the element id from the attribute id.
         *
         * For example: `defense/fire` where
         * - `defense` is the attribute id,
         * - `fire` is the element id,
         * - `/` is the separator
         */
        const val ELEMENT_SEPARATOR = '/'
    }

    /**
     * Instantiates the type using the global attribute config as value providers.
     *
     * This constructor is used if the [bundleId] is different from the [id].
     *
     * @param bundleId the bundle id of attribute to which this attribute is related
     */
    internal constructor(
        id: String,
        bundleId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        element: RegistryEntry<Element>,
        vanilla: Boolean = false,
    ) : this(
        id = id,
        bundleId = bundleId,
        defaultValue = GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(bundleId, "values", element.getIdAsString(), "default").orElse(defaultValue),
        minValue = GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(bundleId, "values", element.getIdAsString(), "min").orElse(minValue),
        maxValue = GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(bundleId, "values", element.getIdAsString(), "max").orElse(maxValue),
        element = element,
        vanilla = vanilla
    )

    internal constructor(
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        element: RegistryEntry<Element>,
        vanilla: Boolean = false,
    ) : this(
        id = descriptionId,
        bundleId = descriptionId,
        defaultValue = defaultValue,
        minValue = minValue,
        maxValue = maxValue,
        element = element,
        vanilla = vanilla
    )

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.concat(
            super.examinableProperties(),
            Stream.of(
                ExaminableProperty.of("element", element)
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is ElementAttribute)
            return this.id == other.id /* && element == other.element */
        return false
    }

    override fun hashCode(): Int {
        val result = this.id.hashCode()
        // result = (31 * result) + element.hashCode()
        return result
    }
}

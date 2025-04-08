package cc.mewcraft.wakame.entity.attribute.bundle

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.commons.provider.Provider
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * 包含了一个 [cc.mewcraft.wakame.entity.attribute.bundle.AttributeBundle] 所相关的各种字段和操作.
 *
 * @param T [ConstantAttributeBundle] 的一个子类
 * @param S [VariableAttributeBundle] 的一个子类
 */
interface AttributeFacade<T : ConstantAttributeBundle, S : VariableAttributeBundle> : Keyed {
    /**
     * 本实例的全局配置文件.
     */
    val config: Provider<ConfigurationNode>

    /**
     * [属性块][cc.mewcraft.wakame.entity.attribute.bundle.AttributeBundle] 的唯一标识.
     *
     * 属性块的唯一标识与单个[属性][cc.mewcraft.wakame.entity.attribute.Attribute] 的唯一标识不一定相同,
     * 当属性块是由多个属性构成时(例如攻击力),
     * 它们的唯一标识就不一样.
     */
    val id: String

    val valueType: KType // KType<T>

    val sourceType: KType // KType<S>

    /**
     * Holds metadata about the attribute components.
     */
    val bundleTrait: AttributeBundleTraitSet

    /**
     * A creator for attribute modifiers.
     */
    val createAttributeModifiers: (Key, T) -> Map<Attribute, AttributeModifier>

    /**
     * A creator for [cc.mewcraft.wakame.item.templates.components.cells.cores.AttributeCoreArchetype].
     */
    val convertNodeToVariable: (ConfigurationNode) -> S

    /**
     * A creator for [cc.mewcraft.wakame.item.components.cells.AttributeCore].
     */
    val convertNodeToConstant: (ConfigurationNode) -> T

    /**
     * A creator for [cc.mewcraft.wakame.item.components.cells.AttributeCore].
     */
    val convertNbtToConstant: (CompoundTag) -> T

    /**
     * A creator for tooltip name.
     */
    val createTooltipName: (T) -> Component

    /**
     * A creator for tooltip lore.
     */
    val createTooltipLore: (T) -> List<Component>
}


/**
 * 一个属性的组件相关信息.
 */
class AttributeBundleTraitSet(
    val components: Set<KClass<out AttributeBundleTrait>>,
) {
    constructor(vararg components: KClass<out AttributeBundleTrait>) : this(components.toHashSet())

    /**
     * 查询该属性是否有指定的组件.
     *
     * @param T 组件的类型
     * @return 如果该属性拥有该组件，则返回 `true`
     */
    inline fun <reified T : AttributeBundleTrait> has(): Boolean {
        return T::class in components
    }
}
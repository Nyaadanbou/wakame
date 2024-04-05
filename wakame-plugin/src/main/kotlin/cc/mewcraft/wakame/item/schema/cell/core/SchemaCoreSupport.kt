@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.AttributeData
import cc.mewcraft.wakame.attribute.facade.SchemaAttributeData
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.BinarySkillCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.RandomizedValue
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import kotlin.math.max

//<editor-fold desc="Schema Attribute Core">
val SchemaAttributeCore.element: Element
    get() = requireNotNull(elementOrNull) { "The 'element' component is not present" }
val SchemaAttributeCore.elementOrNull: Element?
    get() = (this as? AttributeComponent.Element<Element>)?.element
// val SchemaAttributeCore.valueOrNull: RandomizedValue?
//     get() = (this as? AttributeComponent.Single<RandomizedValue>)?.value
// val SchemaAttributeCore.lowerOrNull: RandomizedValue?
//     get() = (this as? AttributeComponent.Ranged<RandomizedValue>)?.lower
// val SchemaAttributeCore.upperOrNull: RandomizedValue?
//     get() = (this as? AttributeComponent.Ranged<RandomizedValue>)?.upper

/**
 * A [SchemaCore] of an attribute.
 */
sealed interface SchemaAttributeCore : SchemaCore, AttributeData, AttributeComponent.Op<Operation> {
    /**
     * The numeric value's tag type.
     *
     * It's used to generate most optimized numeric values for NBT data.
     */
    val tagType: ShadowTagType

    /**
     * Specifically overrides the return type as [BinaryAttributeCore].
     */
    override fun generate(context: SchemaGenerationContext): BinaryAttributeCore

    /**
     * Implementation of [SchemaAttributeData.S].
     */
    data class S(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val value: RandomizedValue,
    ) : SchemaAttributeCore, SchemaAttributeData.S {
        override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
            val factor = context.level
            val value = value.calculate(factor)
            return BinaryAttributeCore.S(key, tagType, operation, value)
        }
    }

    /**
     * Implementation of [SchemaAttributeData.R].
     */
    data class R(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val lower: RandomizedValue,
        override val upper: RandomizedValue,
    ) : SchemaAttributeCore, SchemaAttributeData.R {
        override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
            val factor = context.level
            val lower = lower.calculate(factor)
            val upper = upper.calculate(factor)
            return BinaryAttributeCore.R(key, tagType, operation, lower, max(lower, upper))
        }
    }

    /**
     * Implementation of [SchemaAttributeData.SE].
     */
    data class SE(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val value: RandomizedValue,
        override val element: Element,
    ) : SchemaAttributeCore, SchemaAttributeData.SE {
        override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
            val factor = context.level
            val value = value.calculate(factor)
            return BinaryAttributeCore.SE(key, tagType, operation, value, element)
        }
    }

    /**
     * Implementation of [SchemaAttributeData.RE].
     */
    data class RE(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val lower: RandomizedValue,
        override val upper: RandomizedValue,
        override val element: Element,
    ) : SchemaAttributeCore, SchemaAttributeData.RE {
        override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
            val factor = context.level
            val lower = lower.calculate(factor)
            val upper = upper.calculate(factor)
            return BinaryAttributeCore.RE(key, tagType, operation, lower, max(lower, upper), element)
        }
    }
}
//</editor-fold>

//<editor-fold desc="Schema Skill Core">
/**
 * A [SchemaCore] of a skill.
 */
data class SchemaSkillCore(
    override val key: Key,
) : SchemaCore {
    override fun generate(context: SchemaGenerationContext): BinarySkillCore {
        // 根据设计，技能的数值分为两类：
        // 1) 技能本身的内部数值
        // 2) 技能依赖的外部数值
        // 技能本身的内部数值，按照设计，只有一个 key，无任何其他信息
        // 技能依赖的外部数值，例如属性，魔法值，技能触发时便知道
        // 综上，物品上的技能无需储存除 key 以外的任何数据
        return BinarySkillCore(key)
    }
}
//</editor-fold>
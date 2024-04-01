package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.ability.NoopPlainAbilityData
import cc.mewcraft.wakame.ability.NoopSchemaAbilityData
import cc.mewcraft.wakame.ability.SchemaAbilityData
import cc.mewcraft.wakame.attribute.facade.SchemaAttributeData
import cc.mewcraft.wakame.item.BinaryCoreData
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.key.Key

/**
 * A [SchemaCore] of an attribute.
 */
data class SchemaAttributeCore(
    override val key: Key,
    override val data: SchemaAttributeData,
) : SchemaCore {
    override fun generate(context: SchemaGenerationContext): BinaryCoreData {
        val realizer = AttributeRegistry.FACADES[key].SCHEMA_DATA_REALIZER
        val factor = context.level
        val data = realizer.realize(data, factor)
        return data
    }
}

/**
 * A [SchemaCore] of an ability.
 */
data class SchemaAbilityCore(
    override val key: Key,
) : SchemaCore {
    override val data: SchemaAbilityData = NoopSchemaAbilityData
    override fun generate(context: SchemaGenerationContext): BinaryCoreData {
        // 根据设计，技能的数值分为两类：
        // 1) 技能本身的内部数值
        // 2) 技能依赖的外部数值
        // 技能本身的内部数值，按照设计，只有一个 key，无任何其他信息
        // 技能依赖的外部数值，例如属性，魔法值，技能触发时便知道
        // 综上，物品上的技能无需储存除 key 以外的任何数据
        return NoopPlainAbilityData
    }
}
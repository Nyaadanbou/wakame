package cc.mewcraft.wakame.item.schema.cell.core.skill

import cc.mewcraft.wakame.item.binary.cell.core.BinarySkillCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import net.kyori.adventure.key.Key

/**
 * The dead simple implementation of [SchemaSkillCore].
 */
data class SchemaSkillCoreSimple(
    override val key: Key,
) : SchemaSkillCore {
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

/**
 * The implementation of [SchemaSkillCore] with a specific trigger.
 */
private class SchemaSkillCoreWithTrigger(
    override val key: Key,
    // override val trigger: SkillTrigger, TODO 等 SkillTrigger 接口确定后再写这个
) : SchemaSkillCore {
    override fun generate(context: SchemaGenerationContext): BinarySkillCore {
        TODO("Not yet implemented")
    }
}
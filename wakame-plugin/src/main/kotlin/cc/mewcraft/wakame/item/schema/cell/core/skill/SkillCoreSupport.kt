package cc.mewcraft.wakame.item.schema.cell.core.skill

import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCoreDataHolder
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.filter.SkillContextHolder
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillTrigger
import net.kyori.adventure.key.Key

//
// Internal Implementations
//

internal data class SchemaSkillCoreImpl(
    override val key: Key, // the key will be used to get the Skill instance
    override val trigger: SkillTrigger,
) : SchemaSkillCore {
    override val instance: Skill
        get() = SkillRegistry.INSTANCE[key]

    override fun reify(context: SchemaGenerationContext): BinarySkillCore {
        // 根据设计，技能的数值分为两类：
        // 1) 技能本身的内部数值
        // 2) 技能依赖的外部数值

        // 技能本身的内部数值，按照设计，只有
        // 1) 一个 key
        // 2) 一个 trigger

        // 技能依赖的外部数值，例如属性，魔法值，技能触发时便知道
        // 综上，物品上的技能无需储存除 key 以外的任何数据

        context.skills += SkillContextHolder(key)
        return BinarySkillCoreDataHolder(key, trigger)
    }
}

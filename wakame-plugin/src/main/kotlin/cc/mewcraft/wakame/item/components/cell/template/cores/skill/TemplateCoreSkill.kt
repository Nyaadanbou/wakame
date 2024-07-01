package cc.mewcraft.wakame.item.components.cell.template.cores.skill

import cc.mewcraft.wakame.item.components.cell.cores.skill.CoreSkill
import cc.mewcraft.wakame.item.components.cell.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.SkillContextHolder
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.ConfiguredSkill
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * 构建一个 [TemplateCoreSkill].
 */
fun TemplateCoreSkill(node: ConfigurationNode): TemplateCoreSkill {
    val binarySkill = node.krequire<ConfiguredSkill>()
    val key = binarySkill.key
    val trigger = binarySkill.trigger
    val variant = binarySkill.variant
    val template = TemplateCoreSkillImpl(key, trigger, variant)
    return template
}

/**
 * 代表一个技能核心的模板.
 */
interface TemplateCoreSkill : TemplateCore {
    val skill: Skill
    val trigger: Trigger
    val variant: ConfiguredSkill.Variant

    override fun generate(context: GenerationContext): CoreSkill {
        return CoreSkill(key, trigger, variant)
    }
}

private data class TemplateCoreSkillImpl(
    override val key: Key,
    override val trigger: Trigger,
    override val variant: ConfiguredSkill.Variant,
) : TemplateCoreSkill {
    override val skill: Skill
        get() = SkillRegistry.TYPES[key]

    override fun generate(context: GenerationContext): CoreSkill {
        // 根据设计，技能的数值分为两类：
        // 1) 技能本身的内部数值
        // 2) 技能依赖的外部数值

        // 技能本身的内部数值，按照设计，只有
        // 1) 一个 key
        // 2) 一个 trigger

        // 技能依赖的外部数值，例如属性，魔法值，技能触发时便知道
        // 综上，物品上的技能无需储存除 key 以外的任何数据

        context.skills += SkillContextHolder(key)
        return CoreSkill(key, trigger, variant)
    }
}
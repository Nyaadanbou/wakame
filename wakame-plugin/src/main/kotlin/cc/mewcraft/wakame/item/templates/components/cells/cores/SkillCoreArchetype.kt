package cc.mewcraft.wakame.item.templates.components.cells.cores

import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.item.components.cells.cores.SkillCore
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.SkillContextData
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetype
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.PlayerSkill
import cc.mewcraft.wakame.skill2.Skill
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * 本函数用于构建 [SkillCoreArchetype].
 *
 * @param id 核心的唯一标识, 也就是 [CoreArchetype.id]
 * @param node 包含该核心数据的配置节点
 *
 * @return 构建的 [SkillCoreArchetype]
 */
fun SkillCoreArchetype(
    id: Key,
    node: ConfigurationNode
): SkillCoreArchetype {
    val playerSkill = PlayerSkill(id, node)
    return SimpleSkillCoreArchetype(id, playerSkill)
}

/**
 * 代表一个技能核心 [SkillCore] 的模板.
 */
interface SkillCoreArchetype : CoreArchetype {
    /**
     * 该模板包含的技能.
     */
    val skill: PlayerSkill

    /**
     * 该模板包含的技能所对应的实例.
     */
    val instance: Skill

    /**
     * 生成一个 [SkillCore] 实例.
     *
     * @param context 物品生成的上下文
     * @return 生成的 [SkillCore]
     */
    override fun generate(context: ItemGenerationContext): SkillCore
}

/**
 * [SkillCoreArchetype] 的标准实现.
 */
internal data class SimpleSkillCoreArchetype(
    override val id: Key,
    override val skill: PlayerSkill,
) : SkillCoreArchetype {
    override val instance: Skill
        get() = SkillRegistry.INSTANCES[id]

    override fun generate(context: ItemGenerationContext): SkillCore {
        // 根据设计，技能的数值分为两类：
        // 1) 技能本身的内部数值
        // 2) 技能依赖的外部数值

        // 技能本身的内部数值，按照设计，只有
        // 1) 一个 key
        // 2) 一个 trigger

        // 技能依赖的外部数值，例如属性，魔法值，技能触发时便知道
        // 综上, 物品上的技能无需储存除 key 以外的任何数据

        context.skills += SkillContextData(id)

        return SkillCore(id, skill)
    }
}
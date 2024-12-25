package cc.mewcraft.wakame.item.templates.components.cells.cores

import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.cores.AbilityCore
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.AbilityContextData
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetype
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.Ability
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * 本函数用于构建 [AbilityCoreArchetype].
 *
 * @param id 核心的唯一标识, 也就是 [CoreArchetype.id]
 * @param node 包含该核心数据的配置节点
 *
 * @return 构建的 [AbilityCoreArchetype]
 */
fun AbilityCoreArchetype(
    id: Key,
    node: ConfigurationNode
): AbilityCoreArchetype {
    val playerAbility = PlayerAbility(id, node)
    return SimpleAbilityCoreArchetype(id, playerAbility)
}

/**
 * 代表一个技能核心 [AbilityCore] 的模板.
 */
interface AbilityCoreArchetype : CoreArchetype {
    /**
     * 该模板包含的技能.
     */
    val ability: PlayerAbility

    /**
     * 该模板包含的技能所对应的实例.
     */
    val instance: Ability

    /**
     * 生成一个 [AbilityCore] 实例.
     *
     * @param context 物品生成的上下文
     * @return 生成的 [AbilityCore]
     */
    override fun generate(context: ItemGenerationContext): AbilityCore
}

/**
 * [AbilityCoreArchetype] 的标准实现.
 */
internal data class SimpleAbilityCoreArchetype(
    override val id: Key,
    override val ability: PlayerAbility,
) : AbilityCoreArchetype {
    override val instance: Ability
        get() = AbilityRegistry.INSTANCES[id]

    override fun generate(context: ItemGenerationContext): AbilityCore {
        // 根据设计，技能的数值分为两类：
        // 1) 技能本身的内部数值
        // 2) 技能依赖的外部数值

        // 技能本身的内部数值，按照设计，只有
        // 1) 一个 key
        // 2) 一个 trigger

        // 技能依赖的外部数值，例如属性，魔法值，技能触发时便知道
        // 综上, 物品上的技能无需储存除 key 以外的任何数据

        context.abilities += AbilityContextData(id)

        return AbilityCore(id, ability)
    }
}
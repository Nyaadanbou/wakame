package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import org.bukkit.inventory.ItemStack

sealed interface SkillCastContext {
    val caster: Caster
    val target: Target
    val itemStack: ItemStack?
}

data class VoidSkillCastContext(
    override val caster: Caster.Void,
    override val target: Target,
    override val itemStack: ItemStack? = null
) : SkillCastContext

data class PlayerSkillCastContext(
    override val caster: Caster.Player,
    override val target: Target,
    override val itemStack: ItemStack
) : SkillCastContext

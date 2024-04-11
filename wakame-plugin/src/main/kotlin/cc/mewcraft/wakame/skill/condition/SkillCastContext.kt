package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.skill.Caster
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface SkillCastContext {
    val caster: Caster
    val player: Player
    val itemStack: ItemStack
}

data class SkillCastContextImpl(
    override val caster: Caster,
    override val player: Player,
    override val itemStack: ItemStack
) : SkillCastContext
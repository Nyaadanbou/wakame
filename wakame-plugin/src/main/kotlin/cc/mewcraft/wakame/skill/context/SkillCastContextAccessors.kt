@file:JvmName("SkillCastContextAccessors")

package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import org.bukkit.inventory.ItemStack

fun SkillCastContext.setCaster(caster: Caster) {
    set(SkillCastContextKeys.CASTER, caster)
    when (caster) {
        is Caster.Player -> set(SkillCastContextKeys.CASTER_PLAYER, caster)
        is Caster.Entity -> set(SkillCastContextKeys.CASTER_ENTITY, caster)
    }
}

fun SkillCastContext.setTarget(target: Target) {
    set(SkillCastContextKeys.TARGET, target)
    when (target) {
        is Target.LivingEntity -> set(SkillCastContextKeys.TARGET_LIVING_ENTITY, target)
        is Target.Location -> set(SkillCastContextKeys.TARGET_LOCATION, target)
        is Target.Void -> {
        }
    }
}

fun SkillCastContext.setItemStack(itemStack: ItemStack) {
    set(SkillCastContextKeys.ITEM_STACK, itemStack)
}
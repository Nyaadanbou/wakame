package cc.mewcraft.wakame.skill2.character

import cc.mewcraft.wakame.skill2.context.SkillInput

object TargetUtil {
    fun getEntity(context: SkillInput, casterWhenNull: Boolean = false): Target.LivingEntity? {
        return when (val target = context.target) {
            is Target.LivingEntity -> {
                val casterEntity = context.caster.entity
                val targetEntity = target.bukkitEntity
                if (casterEntity == targetEntity && casterWhenNull) null
                else target
            }
            is Target.Location -> {
                val location = target.bukkitLocation
                location.getNearbyLivingEntities(1.0).firstOrNull()?.let { TargetAdapter.adapt(it) }
            }
            else -> null
        }
    }

    fun getLocation(context: SkillInput, casterWhenNull: Boolean = false): Target.Location? {
        return when (val target = context.target) {
            is Target.Location -> target
            is Target.LivingEntity -> {
                val casterEntity = context.caster.entity
                val targetEntity = target.bukkitEntity
                if (casterEntity == targetEntity && casterWhenNull) null else targetEntity?.location?.let { TargetAdapter.adapt(it) }
            }
            else -> null
        }
    }
}
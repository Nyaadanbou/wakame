package cc.mewcraft.wakame.skill2.character

import cc.mewcraft.wakame.skill2.context.SkillContext

object CasterUtils {
    fun <T : Caster.Single> getCaster(clazz: Class<T>, context: SkillContext): T? {
        val caster = context.caster
        return caster.value(clazz)
            ?: caster.root(clazz)
    }

    inline fun <reified T : Caster.Single> getCaster(context: SkillContext): T? {
        return getCaster(T::class.java, context)
    }
}

object TargetUtil {
    fun getEntity(context: SkillContext, casterWhenNull: Boolean = false): Target.LivingEntity? {
        return when (val target = context.target) {
            is Target.LivingEntity -> {
                val caster = CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity
                val targetEntity = target.bukkitEntity
                if (caster != null && caster == targetEntity && casterWhenNull) null
                else target
            }
            is Target.Location -> {
                val location = target.bukkitLocation
                location.getNearbyLivingEntities(1.0).firstOrNull()?.let { TargetAdapter.adapt(it) }
            }
            else -> null
        }
    }

    fun getLocation(context: SkillContext, casterWhenNull: Boolean = false): Target.Location? {
        return when (val target = context.target) {
            is Target.Location -> target
            is Target.LivingEntity -> {
                val caster = CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity
                val targetEntity = target.bukkitEntity
                if (caster != null && caster == targetEntity && casterWhenNull) null else targetEntity?.location?.let { TargetAdapter.adapt(it) }
            }
            else -> null
        }
    }
}
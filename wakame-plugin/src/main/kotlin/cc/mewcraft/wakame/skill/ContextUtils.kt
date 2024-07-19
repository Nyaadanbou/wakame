package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey

object CasterUtils {
    fun <T : Caster.Single> getCaster(clazz: Class<T>, context: SkillContext): T? {
        val caster = context[SkillContextKey.CASTER]
        return caster?.value(clazz)
            ?: caster?.root(clazz)
    }

    inline fun <reified T : Caster.Single> getCaster(context: SkillContext): T? {
        return getCaster(T::class.java, context)
    }
}

object TargetUtil {
    fun getEntity(context: SkillContext, ignoreCaster: Boolean = false): Target.LivingEntity? {
        return when (val target = context[SkillContextKey.TARGET]) {
            is Target.LivingEntity -> {
                val caster = CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity
                val targetEntity = target.bukkitEntity
                if (caster != null && caster == targetEntity && ignoreCaster) null
                else target
            }
            is Target.Location -> {
                val location = target.bukkitLocation
                location.getNearbyLivingEntities(1.0).firstOrNull()?.let { TargetAdapter.adapt(it) }
            }
            else -> null
        }
    }

    fun getLocation(context: SkillContext, ignoreCaster: Boolean = false): Target.Location? {
        return when (val target = context[SkillContextKey.TARGET]) {
            is Target.Location -> target
            is Target.LivingEntity -> {
                val caster = CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity
                val targetEntity = target.bukkitEntity
                if (caster != null && caster == targetEntity && ignoreCaster) null else targetEntity?.location?.let { TargetAdapter.adapt(it) }
            }
            else -> null
        }
    }
}
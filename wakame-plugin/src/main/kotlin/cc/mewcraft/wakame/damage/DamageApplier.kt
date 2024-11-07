package cc.mewcraft.wakame.damage

import org.bukkit.entity.LivingEntity

object DamageApplier : DamageApplierApi {
    override fun doDamage(victim: LivingEntity, source: LivingEntity?, amount: Double) {
        victim.damage(amount, source)
    }
}
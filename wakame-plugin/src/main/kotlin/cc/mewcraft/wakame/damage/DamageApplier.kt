package cc.mewcraft.wakame.damage

import org.bukkit.entity.LivingEntity

object BukkitDamageApplier : DamageApplier {
    override fun damage(victim: LivingEntity, source: LivingEntity?, amount: Double) {
        // 这里仅仅用于触发一下 Bukkit 的 EntityDamageEvent.
        // 伤害数值填多少都无所谓, 最后都会被事件监听逻辑重新计算.
        victim.damage(amount, source)
    }
}
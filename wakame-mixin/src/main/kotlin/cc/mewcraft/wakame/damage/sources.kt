package cc.mewcraft.wakame.damage

import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Koish 用到的 Bukkit 伤害来源.
 */
object KoishDamageSources{
    fun playerAttack(player: Player): DamageSource {
        return DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(player).withDirectEntity(player).build()
    }

    fun mobAttack(livingEntity: LivingEntity): DamageSource {
        return DamageSource.builder(DamageType.MOB_ATTACK).withCausingEntity(livingEntity).withDirectEntity(livingEntity).build()
    }
}
package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

object DamageManager {
    fun generateMetaData(event: EntityDamageEvent): DamageMetaData {
        //存在造成伤害的实体时
        if (event is EntityDamageByEntityEvent) {
            when (val damager = event.damager) {
                is Player -> {
                    //纯原版的情况下，理论上玩家能够产生的伤害类型只有：
                    //ENTITY_ATTACK
                    //ENTITY_SWEEP_ATTACK
                    //MAGIC
                    return when (event.cause) {
                        DamageCause.ENTITY_ATTACK -> {
                            PlayerMeleeAttackMetaData(damager.toUser())
                        }

                        DamageCause.ENTITY_SWEEP_ATTACK -> TODO("横扫伤害，可能存在额外伤害衰减")

                        //其他伤害类型均视为原版伤害
                        else -> {
                            VanillaDamageMetaData(event.damage)
                        }
                    }
                }

                is LivingEntity -> {
                    return EntityMeleeAttackMetaData(damager)
                }

                is Projectile -> {
                    TODO()
                }
            }
            //如果造成伤害的实体不是LivingEntity也不是弹射物
            //这种情况理论上不应该存在，若出现，视为原版伤害
            return VanillaDamageMetaData(event.damage)
        }
        //如果伤害没有攻击者，视为原版伤害
        //TODO 对一些原版伤害进行修饰，比如加强溺水伤害
        return VanillaDamageMetaData(event.damage)
    }
}
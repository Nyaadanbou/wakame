package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.damage.CustomDamageMetadata
import cc.mewcraft.wakame.damage.DamageTag
import cc.mewcraft.wakame.damage.DamageTags
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.toNekoStack
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 物品发动攻击的逻辑
 * 用于实现各种攻击效果
 */
interface Attack : ItemBehavior {
    private object Default : Attack {
        override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
            val nekoStack = itemStack.toNekoStack
            val attack = nekoStack.templates.get(ItemTemplateTypes.ATTACK) ?: return
            when (attack.attackType) {
                is AxeAttack -> {}
                is BowShoot -> {}
                is CrossbowShoot -> {}

                is HammerAttack -> {
                    val damageMetadata = event.damageMetadata
                    val damageTags = damageMetadata.damageTags
                    if (damageTags.contains(DamageTag.HAMMER) && !damageTags.contains(DamageTag.EXTRA)) {

                        val customDamageMetadata = CustomDamageMetadata(
                            criticalPower = damageMetadata.criticalPower,
                            criticalState = damageMetadata.criticalState,
                            knockback = true,
                            damageBundle = damageMetadata.damageBundle,//TODO 比率属性 范围属性
                            damageTags = DamageTags(DamageTag.MELEE, DamageTag.HAMMER, DamageTag.EXTRA)
                        )
                        damagee.location.getNearbyLivingEntities(3.0).forEach {
                            if (it.uniqueId != player.uniqueId && it.uniqueId != damagee.uniqueId) {
                                it.hurt(customDamageMetadata, player)
                            }
                        }
                    }
                }

                is MaceAttack -> {}
                is SwordAttack -> {}
                is TridentAttack -> {}
            }
        }
    }

    companion object Type : ItemBehaviorType<Attack> {
        override fun create(): Attack {
            return Default
        }
    }
}
package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.damage.KoishDamageSources
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.extension.addCooldown
import cc.mewcraft.wakame.item.extension.damageItem
import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.weapon.checkTwoHandedRequirement
import cc.mewcraft.wakame.item.property.impl.weapon.handleTwoHandedFailure
import org.bukkit.inventory.EquipmentSlot
import kotlin.math.max

object Sword : Weapon {
    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.PASS
        val sword = itemstack.getProp(ItemPropTypes.SWORD) ?: return InteractionResult.PASS

        // 检查双手持握
        if (!sword.checkTwoHandedRequirement(player)) {
            sword.handleTwoHandedFailure(player)
            return InteractionResult.FAIL
        }

        // 造成伤害
        val attrContainer = player.attributeContainer
        val hitEntities = WeaponUtils.getHitEntities(player, sword.attackHalfExtentsBase)
        if (hitEntities.isNotEmpty()) {
            val damageMetadata = if (sword.damageDistributed) {
                val rateMultiplier = max(1.0 / hitEntities.size, sword.minDamageDistributedRatio)
                PlayerDamageMetadata(attrContainer) {
                    every {
                        standard()
                        rate {
                            rateMultiplier * standard()
                        }
                    }
                }
            } else {
                PlayerDamageMetadata(attrContainer) {
                    every {
                        standard()
                    }
                }
            }
            val damageSource = KoishDamageSources.playerAttack(player)

            // 成功造成伤害才扣除耐久
            if (WeaponUtils.hurtEntities(hitEntities, damageMetadata, damageSource, true)) {
                player.damageItem(EquipmentSlot.HAND, sword.itemDamagePerAttack)
            }
        }

        // 设置冷却
        itemstack.addCooldown(player, sword.attackCooldown)
        return InteractionResult.SUCCESS
    }

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        val sword = itemstack.getProp(ItemPropTypes.SWORD) ?: return InteractionResult.PASS

        // 检查双手持握
        if (!sword.checkTwoHandedRequirement(player)) {
            sword.handleTwoHandedFailure(player)
            return InteractionResult.FAIL
        }

        return InteractionResult.PASS
    }
}
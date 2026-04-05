package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.damage.KoishDamageSources
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.extension.addCooldown
import cc.mewcraft.wakame.item.extension.damageItem
import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import org.bukkit.inventory.EquipmentSlot
import kotlin.math.max

object Sword : Weapon {
    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.FAIL_AND_CANCEL
        val sword = itemstack.getProp(ItemPropTypes.SWORD) ?: return InteractionResult.FAIL_AND_CANCEL

        // 不满足双手持握要求
        if (sword.twoHanded && !player.inventory.itemInOffHand.isEmpty) {
            player.sendActionBar(TranslatableMessages.MSG_ERR_REQUIRE_TWO_HANDED.build())
            return InteractionResult.FAIL_AND_CANCEL
        }

        // 造成伤害
        val attrContainer = player.attributeContainer
        val hitEntities = WeaponUtils.getHitEntities(player, sword.attackHalfExtentsBase)
        if (hitEntities.isNotEmpty()) {
            val rateMultiplier = max(1.0 / hitEntities.size, sword.minDamageDistributedRatio)
            val damageMetadata = PlayerDamageMetadata(attrContainer) {
                every {
                    standard()
                    rate { // override
                        rateMultiplier * standard()
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
        return InteractionResult.SUCCESS_AND_CANCEL
    }
}
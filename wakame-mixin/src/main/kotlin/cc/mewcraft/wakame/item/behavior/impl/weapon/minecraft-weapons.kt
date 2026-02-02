package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item.behavior.InteractionHand
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.extension.addCooldown
import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * **原版弓**武器行为.
 *
 * 箭矢伤害计算见 DamageManager 弹射物部分.
 *
 * 相较于原版的改动:
 * - 副手无法使用.
 * - 直接攻击(左键)实体不会有伤害.
 */
object Bow : Weapon {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        // 禁止副手交互弓
        if (context.hand == InteractionHand.OFF_HAND) {
            context.player.sendActionBar(TranslatableMessages.MSG_ERR_CANNOT_USE_BOW_IN_OFFHAND.build())
            return InteractionResult.FAIL_AND_CANCEL
        }
        return InteractionResult.PASS
    }
}

/**
 * **原版弩**武器行为.
 *
 * 箭矢伤害计算见 DamageManager 弹射物部分.
 *
 * 相较于原版的改动:
 * - 副手无法使用.
 * - 直接攻击(左键)实体不会有伤害.
 */
object Crossbow : Weapon {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        // 禁止副手交互弩
        if (context.hand == InteractionHand.OFF_HAND) {
            context.player.sendActionBar(TranslatableMessages.MSG_ERR_CANNOT_USE_CROSSBOW_IN_OFFHAND.build())
            return InteractionResult.FAIL_AND_CANCEL
        }
        return InteractionResult.PASS
    }
}

/**
 * **原版重锤**武器行为.
 *
 * 相较于原版的改动:
 * - 只有造成伤害才会进入冷却.
 * - 可以调整每单位下落高度增加的伤害的系数: [cc.mewcraft.wakame.item.property.impl.weapon.Mace.attackDamageRatePerFallHeight].
 * - 可以设置下落高度增加的额外伤害的上限: [cc.mewcraft.wakame.item.property.impl.weapon.Mace.damageByFallHeightLimit].
 */
object Mace : Weapon {
    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        if (itemstack.isOnCooldown(player)) return null
        val minecraftMace = itemstack.getProp(ItemPropTypes.MINECRAFT_MACE) ?: return null
        itemstack.addCooldown(player, minecraftMace.attackCooldown)
        return PlayerDamageMetadata(player.attributeContainer) {
            every {
                standard()
                rate {
                    val fallHeight = player.fallDistance.toDouble().coerceAtLeast(.0)
                    val extraAttackDamageByFallHeight = fallHeight * minecraftMace.attackDamageRatePerFallHeight
                    val extraAttackDamageByFallHeightCoerced = extraAttackDamageByFallHeight.coerceAtMost(minecraftMace.damageByFallHeightLimit)
                    standard() + extraAttackDamageByFallHeightCoerced
                }
            }
        }
    }
}

/**
 * **原版近战(斧, 镐, 锄等单体武器)**武器行为.
 *
 * 相较于原版的改动:
 * - 只有造成伤害才会进入冷却.
 */
object Melee : Weapon {

    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        if (itemstack.isOnCooldown(player)) return null
        val minecraftMelee = itemstack.getProp(ItemPropTypes.MINECRAFT_MELEE) ?: return null
        itemstack.addCooldown(player, minecraftMelee.attackCooldown)

        return PlayerDamageMetadata(player.attributeContainer) {
            every {
                standard()
            }
        }
    }
}

/**
 * **原版三叉戟**武器行为.
 *
 * 投掷出的三叉戟伤害计算见 DamageManager 弹射物部分.
 *
 * 相较于原版的改动:
 * - 副手无法使用.
 * - 只有造成伤害才会进入冷却.
 */
object Trident : Weapon {
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        // 禁止副手交互三叉戟
        if (context.hand == InteractionHand.OFF_HAND) {
            context.player.sendActionBar(TranslatableMessages.MSG_ERR_CANNOT_USE_TRIDENT_IN_OFFHAND.build())
            return InteractionResult.FAIL_AND_CANCEL
        }
        return InteractionResult.PASS
    }

    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        if (itemstack.isOnCooldown(player)) return null
        val minecraftTrident = itemstack.getProp(ItemPropTypes.MINECRAFT_TRIDENT) ?: return null
        itemstack.addCooldown(player, minecraftTrident.attackCooldown)
        return PlayerDamageMetadata(player.attributeContainer) {
            every {
                standard()
            }
        }
    }
}
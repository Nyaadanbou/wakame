package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.AttackEntityContext
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
 * 原版弓 武器行为.
 * 箭矢伤害计算见 DamageManager 弹射物部分.
 *
 * 相较于原版的改动:
 * 副手无法使用.
 * 直接攻击(左键)实体不会有伤害.
 */
object Bow : Weapon {
    // 禁止副手交互弓
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        if (context.hand == InteractionHand.OFF_HAND) {
            context.player.sendActionBar(TranslatableMessages.MSG_USE_BOW_IN_OFFHAND.build())
            return InteractionResult.FAIL_AND_CANCEL
        }
        return InteractionResult.PASS
    }
}

/**
 * 原版弩 武器行为.
 * 箭矢伤害计算见 DamageManager 弹射物部分.
 *
 * 相较于原版的改动:
 * 副手无法使用.
 * 直接攻击(左键)实体不会有伤害.
 */
object Crossbow : Weapon {
    // 禁止副手交互弩
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        if (context.hand == InteractionHand.OFF_HAND) {
            context.player.sendActionBar(TranslatableMessages.MSG_USE_CROSSBOW_IN_OFFHAND.build())
            return InteractionResult.FAIL_AND_CANCEL
        }
        return InteractionResult.PASS
    }
}

/**
 * 原版重锤 武器行为.
 *
 * 相较于原版的改动:
 * 伤害率会额外增加 下落高度 * [cc.mewcraft.wakame.item.property.impl.weapon.Mace.attackDamageRatePerFallDistance].
 * 增加上限为 [cc.mewcraft.wakame.item.property.impl.weapon.Mace.attackDamageRateLimit].
 */
object Mace : Weapon {
    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        if (itemstack.isOnCooldown(player)) return null
        val mace = itemstack.getProp(ItemPropTypes.MINECRAFT_MACE) ?: return null

        val attrContainer = player.attributeContainer
        val fallDistance = player.fallDistance.toDouble().coerceAtLeast(.0)
        return PlayerDamageMetadata(attrContainer) {
            every {
                standard()
                rate {
                    standard() + (fallDistance * mace.attackDamageRatePerFallDistance).coerceAtMost(mace.attackDamageRateLimit)
                }
            }
        }
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.FAIL

        val mace = itemstack.getProp(ItemPropTypes.MINECRAFT_MACE) ?: return InteractionResult.FAIL
        itemstack.addCooldown(player, mace.attackCooldown)
        return InteractionResult.SUCCESS
    }
}

/**
 * 原版近战(斧, 镐, 锄等单体武器) 武器行为.
 *
 * 相较于原版的改动:
 * 只有攻击(默认左键)实体才会进入冷却.
 * 攻击方块、空气不会进入冷却.
 */
object Melee : Weapon {
    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        if (itemstack.isOnCooldown(player)) return null
        val attrContainer = player.attributeContainer
        return PlayerDamageMetadata(attrContainer) {
            every {
                standard()
            }
        }
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.FAIL

        return InteractionResult.SUCCESS
    }

    override fun handleAttackEntity(context: AttackEntityContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.FAIL

        val melee = itemstack.getProp(ItemPropTypes.MINECRAFT_MELEE) ?: return InteractionResult.FAIL
        itemstack.addCooldown(player, melee.attackCooldown)
        return InteractionResult.SUCCESS
    }
}

/**
 * 原版三叉戟 武器行为.
 * 投掷出的三叉戟伤害计算见 DamageManager 弹射物部分.
 *
 * 相较于原版的改动:
 * 副手无法使用.
 * 只有攻击(默认左键)实体才会进入冷却.
 * 攻击方块、空气不会进入冷却.
 */
object Trident : Weapon {
    // 禁止副手交互三叉戟
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        if (context.hand == InteractionHand.OFF_HAND) {
            context.player.sendActionBar(TranslatableMessages.MSG_USE_TRIDENT_IN_OFFHAND.build())
            return InteractionResult.FAIL_AND_CANCEL
        }
        return InteractionResult.PASS
    }

    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        if (itemstack.isOnCooldown(player)) return null
        val attrContainer = player.attributeContainer
        return PlayerDamageMetadata(attrContainer) {
            every {
                standard()
            }
        }
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.FAIL

        return InteractionResult.SUCCESS
    }

    override fun handleAttackEntity(context: AttackEntityContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.FAIL

        val trident = itemstack.getProp(ItemPropTypes.MINECRAFT_TRIDENT) ?: return InteractionResult.FAIL
        itemstack.addCooldown(player, trident.attackCooldown)
        return InteractionResult.SUCCESS
    }
}
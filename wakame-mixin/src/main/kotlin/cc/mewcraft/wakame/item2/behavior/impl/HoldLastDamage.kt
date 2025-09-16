@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item2.behavior.AttackContext
import cc.mewcraft.wakame.item2.behavior.BehaviorResult
import cc.mewcraft.wakame.item2.behavior.CauseDamageContext
import cc.mewcraft.wakame.item2.behavior.ConsumeContext
import cc.mewcraft.wakame.item2.behavior.DurabilityDecreaseContext
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ReceiveDamageContext
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.getProp
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.maxDamage
import org.bukkit.inventory.ItemStack

/**
 * 保留物品最后耐久(维持在0)的行为.
 * 可实现物品耐久耗尽时变为不可用状态(“损坏”状态)而不是直接消失.
 */
object HoldLastDamage : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemStack = context.itemStack
        val player = context.player
        if (itemStack.isBroken()) {
            val holdLastDamageFlags = itemStack.getProp(ItemPropertyTypes.HOLD_LAST_DAMAGE_FLAGS)
            return if (holdLastDamageFlags?.cancelUse != false) {
                // cancel标记为true时, 中断后续物品行为并取消相应事件.
                // 值得一提的是, 未指定(如直接没有Property)时, 视为true以防止忘记配置标记产生恶性后果.
                player.sendActionBar(holdLastDamageFlags?.msgUse ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_USE)
                InteractionResult.FAIL_AND_CANCEL
            } else if (holdLastDamageFlags.finishUse != false) {
                // finish标记为true时, 仅中断后续物品行为.
                player.sendActionBar(holdLastDamageFlags.msgUse ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_USE)
                InteractionResult.FAIL
            } else {
                InteractionResult.PASS
            }
        } else {
            return InteractionResult.PASS
        }
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemStack = context.itemStack
        val player = context.player
        if (itemStack.isBroken()) {
            val holdLastDamageFlags = itemStack.getProp(ItemPropertyTypes.HOLD_LAST_DAMAGE_FLAGS)
            return if (holdLastDamageFlags?.cancelAttack != false) {
                player.sendActionBar(holdLastDamageFlags?.msgAttack ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_ATTACK)
                InteractionResult.FAIL_AND_CANCEL
            } else if (holdLastDamageFlags.finishAttack != false) {
                player.sendActionBar(holdLastDamageFlags.msgAttack ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_ATTACK)
                InteractionResult.FAIL
            } else {
                InteractionResult.PASS
            }
        } else {
            return InteractionResult.PASS
        }
    }

    override fun handleCauseDamage(context: CauseDamageContext): BehaviorResult {
        val itemStack = context.itemStack
        val player = context.player
        if (itemStack.isBroken()) {
            val holdLastDamageFlags = itemStack.getProp(ItemPropertyTypes.HOLD_LAST_DAMAGE_FLAGS)
            return if (holdLastDamageFlags?.cancelCauseDamage != false) {
                player.sendActionBar(holdLastDamageFlags?.msgCauseDamage ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_CAUSE_DAMAGE)
                BehaviorResult.FINISH_AND_CANCEL
            } else if (holdLastDamageFlags.finishCauseDamage != false) {
                player.sendActionBar(holdLastDamageFlags.msgCauseDamage ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_CAUSE_DAMAGE)
                BehaviorResult.FINISH
            } else {
                BehaviorResult.PASS
            }
        } else {
            return BehaviorResult.PASS
        }
    }

    override fun handleReceiveDamage(context: ReceiveDamageContext): BehaviorResult {
        val itemStack = context.itemStack
        val player = context.player
        if (itemStack.isBroken()) {
            val holdLastDamageFlags = itemStack.getProp(ItemPropertyTypes.HOLD_LAST_DAMAGE_FLAGS)
            // “玩家受到伤害”没有设计cancel标记.
            // 毕竟一般情况下不会去取消玩家受伤事件.
            return if (holdLastDamageFlags?.finishReceiveDamage != false) {
                player.sendActionBar(holdLastDamageFlags?.msgReceiveDamage ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_RECEIVE_DAMAGE)
                BehaviorResult.FINISH
            } else {
                BehaviorResult.PASS
            }
        } else {
            return BehaviorResult.PASS
        }
    }

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val itemStack = context.itemStack
        val player = context.player
        if (itemStack.isBroken()) {
            val holdLastDamageFlags = itemStack.getProp(ItemPropertyTypes.HOLD_LAST_DAMAGE_FLAGS)
            return if (holdLastDamageFlags?.cancelConsume != false) {
                player.sendActionBar(holdLastDamageFlags?.msgConsume ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_CONSUME)
                BehaviorResult.FINISH_AND_CANCEL
            } else if (holdLastDamageFlags.finishConsume != false) {
                player.sendActionBar(holdLastDamageFlags.msgConsume ?: TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_CONSUME)
                BehaviorResult.FINISH
            } else {
                BehaviorResult.PASS
            }
        } else {
            return BehaviorResult.PASS
        }
    }

    override fun handleDurabilityDecrease(context: DurabilityDecreaseContext): BehaviorResult {
        val itemStack = context.itemStack
        val currentDamage = itemStack.damage
        val maxDamage = itemStack.maxDamage
        // 如果物品要损坏了
        if (currentDamage + context.originalDurabilityDecreaseValue >= maxDamage) {
            // 设为 0 耐久
            itemStack.damage = maxDamage
            // 取消掉耐久事件
            return BehaviorResult.FINISH_AND_CANCEL
        } else {
            return BehaviorResult.PASS
        }
    }

    /**
     * 判断物品是否处于"损坏"状态.
     */
    private fun ItemStack.isBroken(): Boolean {
        return this.damage >= this.maxDamage
    }

}
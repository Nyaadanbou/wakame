@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.item2.behavior.AttackContext
import cc.mewcraft.wakame.item2.behavior.BehaviorResult
import cc.mewcraft.wakame.item2.behavior.CauseDamageContext
import cc.mewcraft.wakame.item2.behavior.ConsumeContext
import cc.mewcraft.wakame.item2.behavior.DurabilityDecreaseContext
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContext
import cc.mewcraft.wakame.item2.behavior.ReceiveDamageContext
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.config.property.impl.HoldLastDamageAction
import cc.mewcraft.wakame.item2.config.property.impl.ItemBehaviorHandlerType
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
        return handleInteract(ItemBehaviorHandlerType.SIMPLE_USE, context)
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        return handleInteract(ItemBehaviorHandlerType.SIMPLE_ATTACK, context)
    }

    override fun handleCauseDamage(context: CauseDamageContext): BehaviorResult {
        return handle(ItemBehaviorHandlerType.CAUSE_DAMAGE, context)
    }

    override fun handleReceiveDamage(context: ReceiveDamageContext): BehaviorResult {
        return handle(ItemBehaviorHandlerType.RECEIVE_DAMAGE, context)
    }

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        return handle(ItemBehaviorHandlerType.CONSUME, context)
    }

    override fun handleDurabilityDecrease(context: DurabilityDecreaseContext): BehaviorResult {
        val itemstack = context.itemstack
        val currentDamage = itemstack.damage
        val maxDamage = itemstack.maxDamage
        // 如果物品要损坏了
        if (currentDamage + context.originalDurabilityDecreaseValue >= maxDamage) {
            // 设为 0 耐久
            itemstack.damage = maxDamage
            // 取消掉耐久事件
            return BehaviorResult.FINISH_AND_CANCEL
        } else {
            return BehaviorResult.PASS
        }
    }

    /**
     * 方便函数.
     */
    private fun handleInteract(type: ItemBehaviorHandlerType, context: ItemBehaviorContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (!itemstack.isBroken()) return InteractionResult.PASS

        val settings = itemstack.getProp(ItemPropertyTypes.HOLD_LAST_DAMAGE_SETTINGS)
        val action = settings?.getAction(type) ?: type.defaultAction
        val message = settings?.getMessage(type) ?: type.defaultMessage

        // 向玩家发送动作栏信息
        if (action != HoldLastDamageAction.NONE) {
            player.sendActionBar(message)
        }

        return when (action) {
            HoldLastDamageAction.NONE -> InteractionResult.PASS
            HoldLastDamageAction.FINISH -> InteractionResult.FAIL
            HoldLastDamageAction.CANCEL -> InteractionResult.FAIL_AND_CANCEL
        }
    }

    /**
     * 方便函数.
     */
    private fun handle(type: ItemBehaviorHandlerType, context: ItemBehaviorContext): BehaviorResult {
        val itemstack = context.itemstack
        val player = context.player
        if (!itemstack.isBroken()) return BehaviorResult.PASS

        val settings = itemstack.getProp(ItemPropertyTypes.HOLD_LAST_DAMAGE_SETTINGS)
        val action = settings?.getAction(type) ?: type.defaultAction
        val message = settings?.getMessage(type) ?: type.defaultMessage

        // 向玩家发送动作栏信息
        if (action != HoldLastDamageAction.NONE) {
            player.sendActionBar(message)
        }

        return when (action) {
            HoldLastDamageAction.NONE -> BehaviorResult.PASS
            HoldLastDamageAction.FINISH -> BehaviorResult.FINISH
            HoldLastDamageAction.CANCEL -> BehaviorResult.FINISH_AND_CANCEL
        }
    }

    /**
     * 判断物品是否处于"损坏"状态.
     */
    private fun ItemStack.isBroken(): Boolean {
        return this.damage >= this.maxDamage
    }

}
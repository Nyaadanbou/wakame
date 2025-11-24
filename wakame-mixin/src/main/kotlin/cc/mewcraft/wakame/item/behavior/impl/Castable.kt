package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.ComboCastableTrigger
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger

object Castable : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE)
            ?: return InteractionResult.PASS
        if (castable.trigger.unwrap() == GenericCastableTrigger.RIGHT_CLICK) {
            // 触发 generic/right_click
            val player = context.player
            val manaCost = castable.manaCost
            if (PlayerManaIntegration.consumeMana(player, manaCost)) {
                castable.skill.cast(player)
            }
        } else if (castable.trigger.unwrap() is ComboCastableTrigger) {
            // 触发 combo
            // TODO 积累 combo
            // TODO 检查当前积累的 combo 是否有3个
            // TODO 如果有3个, 则看是否匹配当前序列; 匹配则释放, 不匹配则清空
        }

        return InteractionResult.SUCCESS_AND_CANCEL
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE)
            ?: return InteractionResult.PASS
        if (castable.trigger.unwrap() == GenericCastableTrigger.LEFT_CLICK) {
            // 触发 generic/left_click
            val player = context.player
            val manaCost = castable.manaCost
            if (PlayerManaIntegration.consumeMana(player, manaCost)) {
                castable.skill.cast(player)
            }
        } else if (castable.trigger.unwrap() is ComboCastableTrigger) {
            // 触发 combo
            // TODO 积累 combo
            // TODO 检查当前积累的 combo 是否有3个
            // TODO 如果有3个, 则看是否匹配当前序列; 匹配则释放, 不匹配则清空
        }

        return InteractionResult.SUCCESS_AND_CANCEL
    }
}
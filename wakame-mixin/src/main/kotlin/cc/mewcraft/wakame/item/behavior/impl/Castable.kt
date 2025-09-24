package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.ability.combo.PlayerCombo
import cc.mewcraft.wakame.ability.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext

object Castable : SimpleInteract {
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val player = context.player
        player.koishify()[PlayerCombo.Companion].handleTrigger(AbilitySingleTrigger.RIGHT_CLICK)
        return InteractionResult.SUCCESS_AND_CANCEL
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val player = context.player
        player.koishify()[PlayerCombo.Companion].handleTrigger(AbilitySingleTrigger.LEFT_CLICK)
        return InteractionResult.SUCCESS_AND_CANCEL
    }
}
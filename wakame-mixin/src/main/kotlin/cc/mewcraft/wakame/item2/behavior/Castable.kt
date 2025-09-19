package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.ability2.combo.PlayerCombo
import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item2.behavior.impl.SimpleInteract

object Castable : SimpleInteract {
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val player = context.player
        player.koishify()[PlayerCombo].handleTrigger(AbilitySingleTrigger.RIGHT_CLICK)
        return InteractionResult.SUCCESS_AND_CANCEL
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val player = context.player
        player.koishify()[PlayerCombo].handleTrigger(AbilitySingleTrigger.LEFT_CLICK)
        return InteractionResult.SUCCESS_AND_CANCEL
    }
}
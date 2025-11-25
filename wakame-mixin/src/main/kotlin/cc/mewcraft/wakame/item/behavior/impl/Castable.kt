package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.skill.SkillIntegration
import cc.mewcraft.wakame.integration.skill.SkillWrapper
import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.CastableTrigger
import cc.mewcraft.wakame.item.property.impl.ComboCastableTrigger
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger
import org.bukkit.entity.Player
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp

object Castable : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS

        handleTrigger(context.player, castable, GenericCastableTrigger.RIGHT_CLICK)
        handleTriggerOff(context.player, castable, GenericCastableTrigger.RIGHT_CLICK)

        return InteractionResult.SUCCESS_AND_CANCEL
    }

    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS

        handleTrigger(context.player, castable, GenericCastableTrigger.LEFT_CLICK)
        handleTriggerOff(context.player, castable, GenericCastableTrigger.LEFT_CLICK)

        return InteractionResult.SUCCESS_AND_CANCEL
    }

    //region 处理机制施放部分的逻辑

    private fun handleTrigger(player: Player, castable: CastableProp, expected: CastableTrigger) {
        val trigger = castable.trigger.unwrap()
        when (trigger) {
            expected -> {
                tryPerformCast(player, castable)
            }

            is ComboCastableTrigger -> {
                // 触发 combo
                // TODO 积累 combo
                //   检查当前积累的 combo 是否有3个
                //   如果有3个, 则看是否匹配当前序列; 匹配则释放, 不匹配则清空
            }

            else -> {}
        }
    }

    private fun tryPerformCast(player: Player, castable: CastableProp) {
        val mana = castable.manaCost
        val skill = castable.skill
        if (skill is SkillWrapper.Block) {
            if (
                SkillIntegration.isCooldown(player, skill.id).not() &&
                PlayerManaIntegration.consumeMana(player, mana)
            ) {
                skill.cast(player)
            }
        } else {
            if (PlayerManaIntegration.consumeMana(player, mana)) {
                skill.cast(player)
            }
        }
    }

    //endregion

    //region 处理机制“失效”时施放部分的逻辑
    // 函数和变量在命名上都带了 off 以示区分

    private fun handleTriggerOff(player: Player, castable: CastableProp, expected: CastableTrigger) {
        val trigger = castable.trigger.unwrap()
        when (trigger) {
            expected -> {
                tryPerformCastOff(player, castable)
            }

            is ComboCastableTrigger -> {
                // TODO 触发 combo
            }

            else -> {

            }
        }
    }

    private fun tryPerformCastOff(player: Player, castable: CastableProp) {
        val skillOff = castable.skillOff
        skillOff?.cast(player)
    }

    //endregion
}
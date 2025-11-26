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
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger
import cc.mewcraft.wakame.item.property.impl.SequenceCastableTrigger
import org.bukkit.entity.Player
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp

object Castable : SimpleInteract {

    // 用来实现 castable 中的以下触发器:
    // - generic/right_click
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, GenericCastableTrigger.RIGHT_CLICK)
        }

        return InteractionResult.SUCCESS_AND_CANCEL
    }

    // 用来实现 castable 中的以下触发器:
    // - generic/left_click
    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, GenericCastableTrigger.LEFT_CLICK)
        }

        return InteractionResult.SUCCESS_AND_CANCEL
    }

    //region 处理机制施放部分的逻辑

    private fun handleTrigger(player: Player, castable: CastableProp, expected: CastableTrigger) {
        val trigger = castable.trigger.unwrap()
        when (trigger) {
            expected -> {
                val skill = castable.skill
                val manaCost = castable.manaCost
                if (skill is SkillWrapper.Block) {
                    if (
                        SkillIntegration.isCooldown(player, skill.id, castable).not() &&
                        PlayerManaIntegration.consumeMana(player, manaCost)
                    ) {
                        skill.cast(player, castable)
                    }
                } else {
                    if (PlayerManaIntegration.consumeMana(player, manaCost)) {
                        skill.cast(player, castable)
                    }
                }
            }

            is SequenceCastableTrigger -> {
                // 触发 combo
                // TODO 积累 combo
                //   检查当前积累的 combo 是否有3个
                //   如果有3个, 则看是否匹配当前序列; 匹配则释放, 不匹配则清空
            }

            else -> {}
        }
    }

    //endregion
}
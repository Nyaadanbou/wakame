package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.*
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.CastableTrigger
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger
import cc.mewcraft.wakame.item.property.impl.SpecialCastableTrigger
import cc.mewcraft.wakame.item.tryCastSkill
import org.bukkit.entity.Player
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp

/**
 * 可施法物品的行为实现.
 *
 * 该行为负责处理物品上定义的 [CastableProp] 条目, 根据玩家的交互动作
 * (左键/右键/消耗) 匹配对应的 [CastableTrigger], 并触发技能施放.
 *
 * 支持的触发类型:
 * - [GenericCastableTrigger.LEFT_CLICK] — 左键单击 (攻击)
 * - [GenericCastableTrigger.RIGHT_CLICK] — 右键单击 (使用)
 * - [SpecialCastableTrigger.ON_CONSUME] — 物品被消耗时
 * - [SequenceComboHandler] — 组合键序列 (由外部模块处理)
 */
object Castable : SimpleInteract {

    /**
     * 组合键序列的处理器. 由外部模块 (wakame-plugin) 注入实现.
     *
     * @see SequenceComboHandler
     */
    var sequenceComboHandler: SequenceComboHandler? = null

    // Implements the following triggers in castable:
    // - generic/right_click
    // - sequence/1
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, GenericCastableTrigger.RIGHT_CLICK)
        }

        // 处理组合键序列输入
        sequenceComboHandler?.handleInput(player, castable, GenericCastableTrigger.RIGHT_CLICK)

        return InteractionResult.PASS
    }

    // Implements the following triggers in castable:
    // - generic/left_click
    // - sequence/0
    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, GenericCastableTrigger.LEFT_CLICK)
        }

        // 处理组合键序列输入
        sequenceComboHandler?.handleInput(player, castable, GenericCastableTrigger.LEFT_CLICK)

        return InteractionResult.PASS
    }

    // Implements the following triggers in castable:
    // - special/on_consume
    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return BehaviorResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, SpecialCastableTrigger.ON_CONSUME)
        }

        return BehaviorResult.PASS
    }

    /**
     * 单个 castable 条目的触发匹配与技能施放.
     *
     * 将条目中定义的触发器与期望的触发器进行比较,
     * 若匹配则调用 [tryCastSkill] 执行冷却/魔力检查并施放技能.
     *
     * @param player 执行交互的玩家
     * @param castable 物品上的单个 castable 条目
     * @param expected 本次交互对应的期望触发器
     */
    private fun handleTrigger(player: Player, castable: CastableProp, expected: CastableTrigger) {
        val trigger = castable.trigger.unwrap()
        if (trigger == expected) {
            tryCastSkill(player, castable)
        }
    }
}
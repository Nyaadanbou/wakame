package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.item2.behavior.AttackContext
import cc.mewcraft.wakame.item2.behavior.AttackEntityContext
import cc.mewcraft.wakame.item2.behavior.AttackOnContext
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.behavior.UseEntityContext
import cc.mewcraft.wakame.item2.behavior.UseOnContext

/**
 * 方便接口.
 * 用于不细分交互场景时的物品行为.
 */
interface SimpleInteractBehavior : ItemBehavior {
    /**
     * 玩家手持该物品按下使用键(默认为鼠标右键)进行交互执行的行为.
     * 即无论是对空气, 对方块, 还是对实体使用, 均调用.
     */
    fun handleSimpleUse(context: UseContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品按下攻击键(默认为鼠标左键)进行交互执行的行为.
     * 即无论是对空气, 对方块, 还是对实体攻击, 均调用.
     */
    fun handleSimpleAttack(context: AttackContext): InteractionResult = InteractionResult.PASS

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        return handleSimpleUse(UseContext(context.player, context.hand, context.itemStack))
    }

    override fun handleUse(context: UseContext): InteractionResult {
        return handleSimpleUse(context)
    }

    override fun handleUseEntity(context: UseEntityContext): InteractionResult {
        return handleSimpleUse(UseContext(context.player, context.hand, context.itemStack))
    }

    override fun handleAttackOn(context: AttackOnContext): InteractionResult {
        return handleSimpleAttack(AttackContext(context.player, context.itemStack))
    }

    override fun handleAttack(context: AttackContext): InteractionResult {
        return handleSimpleAttack(context)
    }

    override fun handleAttackEntity(context: AttackEntityContext): InteractionResult {
        return handleSimpleAttack(AttackContext(context.player, context.itemStack))
    }
}
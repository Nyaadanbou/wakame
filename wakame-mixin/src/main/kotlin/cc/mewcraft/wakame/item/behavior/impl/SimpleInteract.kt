package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.AttackEntityContext
import cc.mewcraft.wakame.item.behavior.AttackOnContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.behavior.UseEntityContext
import cc.mewcraft.wakame.item.behavior.UseOnContext

/**
 * 方便接口.
 * 用于不细分交互场景时的物品行为.
 */
interface SimpleInteract : ItemBehavior {
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
        // 此次交互触发了方块交互 - 交互失败
        // SimpleInteract 的目的是简化代码, 因为大量物品的交互行为是只考虑物品本身, 不管到底是点击空气, 还是方块, 亦或是实体
        // 所以此处考虑最常见的情况: 即如果方块具有交互, 则不执行物品的交互
        // 如果某个物品设计需求, 其交互优先级比方块交互还高, 那这种情况显然不常见也不 Simple
        // 此时应该单独继承 ItemBehavior 顶级接口, 分三种情况(空气/方块/实体)处理交互, 而不是使用此方便接口
        if (context.isTriggerBlockInteract) return InteractionResult.FAIL

        return handleSimpleUse(UseContext(context.player, context.itemstack, context.hand))
    }

    override fun handleUse(context: UseContext): InteractionResult {
        return handleSimpleUse(context)
    }

    override fun handleUseEntity(context: UseEntityContext): InteractionResult {
        // 此次交互触发了实体交互 - 交互失败
        // 理由同 handleUseOn
        if (context.isTriggerEntityInteract) return InteractionResult.FAIL
        return handleSimpleUse(UseContext(context.player, context.itemstack, context.hand))
    }

    override fun handleAttackOn(context: AttackOnContext): InteractionResult {
        return handleSimpleAttack(AttackContext(context.player, context.itemstack))
    }

    override fun handleAttack(context: AttackContext): InteractionResult {
        return handleSimpleAttack(context)
    }

    override fun handleAttackEntity(context: AttackEntityContext): InteractionResult {
        return handleSimpleAttack(AttackContext(context.player, context.itemstack))
    }
}
package cc.mewcraft.wakame.hook.impl.craftengine.item.behavior

import cc.mewcraft.wakame.hook.impl.craftengine.toCraftEngine
import cc.mewcraft.wakame.hook.impl.craftengine.toKoish
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.behavior.UseOnContext
import cc.mewcraft.wakame.item2.config.property.ItemPropTypes
import cc.mewcraft.wakame.item2.getProp
import net.momirealms.craftengine.bukkit.item.behavior.LiquidCollisionBlockItemBehavior

object CraftEnginePlaceLiquidCollisionBlock : ItemBehavior {

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        // 未指定方块 ID 和放置高度 - 交互失败
        val settings = context.itemstack.getProp(ItemPropTypes.PLACE_LIQUID_COLLISION_BLOCK) ?: return InteractionResult.FAIL

        // 调用 CE 的物品行为
        val result = LiquidCollisionBlockItemBehavior(settings.blockId.toCraftEngine(), settings.offset).useOnBlock(context.toCraftEngine())
        return result.toKoish()
    }

    override fun handleUse(context: UseContext): InteractionResult {
        // 未指定方块 ID 和放置高度 - 交互失败
        val settings = context.itemstack.getProp(ItemPropTypes.PLACE_LIQUID_COLLISION_BLOCK) ?: return InteractionResult.FAIL

        // 调用 CE 的物品行为
        val cePlayer = context.player.toCraftEngine()
        val result = LiquidCollisionBlockItemBehavior(settings.blockId.toCraftEngine(), 1).use(cePlayer.world(), cePlayer, context.hand.toCraftEngine())
        return result.toKoish()
    }
}
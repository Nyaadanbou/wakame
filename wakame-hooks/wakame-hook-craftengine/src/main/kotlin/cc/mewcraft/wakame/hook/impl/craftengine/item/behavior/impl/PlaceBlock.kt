package cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl

import cc.mewcraft.wakame.hook.impl.craftengine.CraftEngineHook
import cc.mewcraft.wakame.hook.impl.craftengine.toCraftEngine
import cc.mewcraft.wakame.hook.impl.craftengine.toKoish
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.UseOnContext
import cc.mewcraft.wakame.item2.getProp
import net.momirealms.craftengine.bukkit.item.behavior.BlockItemBehavior

object PlaceBlock : ItemBehavior {
    override fun handleUseOn(context: UseOnContext): InteractionResult {
        // 未指定方块id - 交互失败
        val blockId = context.itemstack.getProp(CraftEngineHook.ExtraItemPropTypes.PLACE_BLOCK) ?: return InteractionResult.FAIL

        // 调用ce的物品行为
        val result = BlockItemBehavior(blockId.toCraftEngine()).useOnBlock(context.toCraftEngine())
        return result.toKoish()
    }
}
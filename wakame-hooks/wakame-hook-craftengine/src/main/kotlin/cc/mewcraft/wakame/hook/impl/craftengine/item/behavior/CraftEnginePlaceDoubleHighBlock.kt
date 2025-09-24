package cc.mewcraft.wakame.hook.impl.craftengine.item.behavior

import cc.mewcraft.wakame.hook.impl.craftengine.toCraftEngine
import cc.mewcraft.wakame.hook.impl.craftengine.toKoish
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseOnContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import net.momirealms.craftengine.bukkit.item.behavior.DoubleHighBlockItemBehavior

object CraftEnginePlaceDoubleHighBlock : ItemBehavior {

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        // 未指定方块 ID - 交互失败
        val blockId = context.itemstack.getProp(ItemPropTypes.PLACE_DOUBLE_HIGH_BLOCK) ?: return InteractionResult.FAIL

        // 调用 CE 的物品行为
        val result = DoubleHighBlockItemBehavior(blockId.toCraftEngine()).useOnBlock(context.toCraftEngine())
        return result.toKoish()
    }
}
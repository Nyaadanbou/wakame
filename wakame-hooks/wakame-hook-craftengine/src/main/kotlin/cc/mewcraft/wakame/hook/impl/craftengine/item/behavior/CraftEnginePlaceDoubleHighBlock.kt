package cc.mewcraft.wakame.hook.impl.craftengine.item.behavior

import cc.mewcraft.wakame.hook.impl.craftengine.toCraftEngine
import cc.mewcraft.wakame.hook.impl.craftengine.toKoish
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.UseOnContext
import cc.mewcraft.wakame.item2.config.property.ItemPropTypes
import cc.mewcraft.wakame.item2.getProp
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
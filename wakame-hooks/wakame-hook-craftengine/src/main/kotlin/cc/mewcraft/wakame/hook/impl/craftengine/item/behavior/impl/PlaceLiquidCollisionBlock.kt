package cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl

import cc.mewcraft.wakame.hook.impl.craftengine.CraftEngineHook
import cc.mewcraft.wakame.hook.impl.craftengine.toCraftEngine
import cc.mewcraft.wakame.hook.impl.craftengine.toKoish
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.UseOnContext
import cc.mewcraft.wakame.item2.getProp
import net.momirealms.craftengine.bukkit.item.behavior.LiquidCollisionBlockItemBehavior

object PlaceLiquidCollisionBlock : ItemBehavior {
    override fun handleUseOn(context: UseOnContext): InteractionResult {
        // 未指定方块id和放置高度 - 交互失败
        val blockId = context.itemstack.getProp(CraftEngineHook.ExtraItemPropTypes.PLACE_LIQUID_COLLISION_BLOCK) ?: return InteractionResult.FAIL

        // 调用ce的物品行为
        // FIXME 目前hook中无法调用配置文件相关库, 导致无法反序列化自定义数据类
        val result = LiquidCollisionBlockItemBehavior(blockId.toCraftEngine(), 1).useOnBlock(context.toCraftEngine())
        return result.toKoish()
    }
}
package cc.mewcraft.wakame.item2.behavior.impl.hook.craftengine

import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.behavior.UseOnContext

object PlaceLiquidCollisionBlock : ItemBehavior {

    @get:JvmName("getInstance")
    var INSTANCE: ItemBehavior = ItemBehavior.NO_OP
        private set

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        return INSTANCE.handleUseOn(context)
    }

    override fun handleUse(context: UseContext): InteractionResult {
        return INSTANCE.handleUse(context)
    }

    /**
     * 替换 [PlaceLiquidCollisionBlock] 物品行为的实现.
     */
    fun register(itemBehavior: ItemBehavior) {
        INSTANCE = itemBehavior
    }
}
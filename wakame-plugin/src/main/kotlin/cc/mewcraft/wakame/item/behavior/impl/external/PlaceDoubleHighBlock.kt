package cc.mewcraft.wakame.item.behavior.impl.external

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseOnContext

object PlaceDoubleHighBlock : ItemBehavior {

    private var implementation: ItemBehavior? = null

    /**
     * 替换 [PlaceDoubleHighBlock] 物品行为的实现.
     */
    fun setImplementation(itemBehavior: ItemBehavior) {
        implementation = itemBehavior
    }

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        return implementation?.handleUseOn(context) ?: super.handleUseOn(context)
    }
}
package cc.mewcraft.wakame.item.behavior.impl.external

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.behavior.impl.SimpleInteract

object OpenExternalMenu : SimpleInteract {

    private var implementation: SimpleInteract? = null

    /**
     * 替换 [OpenExternalMenu] 物品行为的实现.
     */
    fun setImplementation(itemBehavior: SimpleInteract) {
        implementation = itemBehavior
    }

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        return implementation?.handleSimpleUse(context) ?: super.handleSimpleUse(context)
    }
}
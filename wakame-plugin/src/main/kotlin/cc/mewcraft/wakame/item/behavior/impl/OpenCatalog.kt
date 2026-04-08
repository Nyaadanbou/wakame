package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext

object OpenCatalog : SimpleInteract {

    private var implementation: SimpleInteract? = null

    fun setImplementation(impl: SimpleInteract) {
        implementation = impl
    }

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        return implementation?.handleSimpleUse(context) ?: super.handleSimpleUse(context)
    }
}
package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

@InternalApi
internal data object EmptyFilter : Filter {
    override val invert: Boolean = false
    override fun test0(context: SchemeGenerationContext): Boolean = false
}
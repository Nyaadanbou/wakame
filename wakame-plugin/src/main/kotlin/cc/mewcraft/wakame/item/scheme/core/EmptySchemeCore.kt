package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

@InternalApi
internal data object EmptySchemeCore : SchemeCore {
    override val key: Nothing get() = throw UnsupportedOperationException("EmptySchemeCore has no key")
    override val value: Nothing get() = throw UnsupportedOperationException("EmptySchemeCore has no value")
    override fun generate(context: SchemeGenerationContext): Nothing = throw UnsupportedOperationException("EmptySchemeCore can't generate anything")
}
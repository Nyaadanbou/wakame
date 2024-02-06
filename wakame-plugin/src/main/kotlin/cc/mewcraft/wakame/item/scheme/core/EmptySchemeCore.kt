package cc.mewcraft.wakame.item.scheme.core

internal data object EmptySchemeCore : SchemeCore {
    override val key: Nothing get() = throw UnsupportedOperationException("EmptySchemeCore has no key")
    override val value: Nothing get() = throw UnsupportedOperationException("EmptySchemeCore has no value")

    override fun generate(scalingFactor: Int): Nothing = throw UnsupportedOperationException("EmptySchemeCore can't generate anything")
}
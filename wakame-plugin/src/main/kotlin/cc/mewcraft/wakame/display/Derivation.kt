package cc.mewcraft.wakame.display

/**
 * 代表内容的衍生规则。
 */
interface Derivation {
    companion object {
        /**
         * A derivation that derives nothing.
         */
        val EMPTY: Derivation = object : Derivation {}
    }
}
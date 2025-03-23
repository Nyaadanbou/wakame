package cc.mewcraft.wakame.enchantment2.metadata

interface EnchantmentMetaType<U, V> {

    companion object {

        fun <U, V> create(): EnchantmentMetaType<U, V> {
            return Simple()
        }

    }

    private class Simple<U, V>() : EnchantmentMetaType<U, V>

}
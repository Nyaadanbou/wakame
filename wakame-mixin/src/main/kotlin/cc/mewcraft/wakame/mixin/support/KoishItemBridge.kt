package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.wakame.util.MojangEnchantment
import cc.mewcraft.wakame.util.MojangStack

interface KoishItemBridge {

    companion object Impl : KoishItemBridge {
        private var implementation: KoishItemBridge = object : KoishItemBridge {
            override fun isKoish(stack: MojangStack): Boolean = false
            override fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = false
            override fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = false
        }

        fun setImplementation(implementation: KoishItemBridge) {
            this.implementation = implementation
        }

        override fun isKoish(stack: MojangStack): Boolean = implementation.isKoish(stack)
        override fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = implementation.isPrimaryEnchantment(stack, enchantment)
        override fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = implementation.isSupportedEnchantment(stack, enchantment)
    }

    fun isKoish(stack: MojangStack): Boolean
    fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean
    fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean
}
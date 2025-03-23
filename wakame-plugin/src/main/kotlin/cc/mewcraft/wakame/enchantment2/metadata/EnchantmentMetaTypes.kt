package cc.mewcraft.wakame.enchantment2.metadata

import cc.mewcraft.wakame.enchantment2.component.AutoMelting
import cc.mewcraft.wakame.enchantment2.component.BlastMining
import cc.mewcraft.wakame.enchantment2.component.Fragile
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentAttributeEffect
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentAutoMeltingEffect
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentBlastMiningEffect
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentFragileEffect

/**
 * 用来将 NMS 的 魔咒效果组件与 Koish 的运行逻辑联系起来.
 */
data object EnchantmentMetaTypes {

    private val registry: HashMap<EnchantmentMetaType<*,*>, EnchantmentMeta<*,*>> = HashMap()

    // ------------

    @JvmField
    val ATTRIBUTES: EnchantmentMetaType<EnchantmentAttributeEffect, Nothing> = typeOf("attributes")

    @JvmField
    val AUTO_MELTING: EnchantmentMetaType<EnchantmentAutoMeltingEffect, AutoMelting> = typeOf("auto_melting")

    @JvmField
    val BLAST_MINING: EnchantmentMetaType<EnchantmentBlastMiningEffect, BlastMining> = typeOf("blast_mining")

    @JvmField
    val FRAGILE: EnchantmentMetaType<EnchantmentFragileEffect, Fragile> = typeOf("fragile")

    // ------------

    fun <U, V> getMeta(effect: U): EnchantmentMeta<U, V> {
        TODO("#365: 好像不需要, 因为无法获取类型信息, 只能传 Any")
    }

    fun getMeta(effect: Any): EnchantmentMeta<Any, Any> {

    }

    private fun <U, V> typeOf(id: String): EnchantmentMetaType<U, V> {
        return EnchantmentMetaType.create()
    }

}
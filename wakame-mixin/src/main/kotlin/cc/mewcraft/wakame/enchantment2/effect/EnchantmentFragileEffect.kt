package cc.mewcraft.wakame.enchantment2.effect

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

@JvmRecord
data class EnchantmentFragileEffect(
    val multiplier: Int,
) {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentFragileEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                ExtraCodecs.intRange(1, 99).fieldOf("multiplier").forGetter(EnchantmentFragileEffect::multiplier)
            ).apply(instance, ::EnchantmentFragileEffect)
        }

    }

}
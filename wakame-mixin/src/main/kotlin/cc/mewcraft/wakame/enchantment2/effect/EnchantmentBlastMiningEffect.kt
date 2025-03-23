package cc.mewcraft.wakame.enchantment2.effect

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

@JvmRecord
data class EnchantmentBlastMiningEffect(
    val explodeLevel: Int,
) {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentBlastMiningEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                ExtraCodecs.intRange(1, 16).fieldOf("explode_level").forGetter(EnchantmentBlastMiningEffect::explodeLevel)
            ).apply(instance, ::EnchantmentBlastMiningEffect)
        }

    }

}
package cc.mewcraft.wakame.enchantment2.effect

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

// FIXME #365: 与 datapack 里的魔咒定义绑定的数据类型.
//  在 datapack 里的 id 可以是比如说 “koish:auto_melting”
@JvmRecord
data class EnchantmentAutoMeltingEffect(
    val activated: Boolean,
) {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentAutoMeltingEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("activated").forGetter(EnchantmentAutoMeltingEffect::activated)
            ).apply(instance, ::EnchantmentAutoMeltingEffect)
        }
    }

}
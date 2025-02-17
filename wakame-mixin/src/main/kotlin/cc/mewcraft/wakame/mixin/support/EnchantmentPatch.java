package cc.mewcraft.wakame.mixin.support;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

public interface EnchantmentPatch {

    /**
     * 该 {@code Codec} 与 {@link Enchantment#DIRECT_CODEC} 不同的是:
     * 序列化时 {@link Enchantment#effects()} 始终返回 {@link DataComponentMap#EMPTY}.
     */
    Codec<Enchantment> PARTIAL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            ComponentSerialization.CODEC.fieldOf("description").forGetter(Enchantment::description),
                            Enchantment.EnchantmentDefinition.CODEC.forGetter(Enchantment::definition),
                            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT)
                                    .optionalFieldOf("exclusive_set", HolderSet.direct())
                                    .forGetter(Enchantment::exclusiveSet),
                            EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", DataComponentMap.EMPTY)
                                    // 序列化时 (encode) 始终返回空的 effects
                                    .forGetter(xenchantment -> DataComponentMap.EMPTY)
                    )
                    .apply(instance, Enchantment::new)
    );

}

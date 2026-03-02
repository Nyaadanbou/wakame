package cc.mewcraft.wakame.mixin.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 修改 {@link RegistryDataLoader.RegistryData} 的构造器,
 * 当 key 为 {@link Registries#ENCHANTMENT} 时替换其 Codec.
 * <p>
 * 使得在向客户端发送 {@link Enchantment} 时, 始终把 {@link Enchantment#effects()}
 * 当成是 {@link DataComponentMap#EMPTY}.
 * <p>
 * 这是因为我们添加了新的项到 {@link Registries#ENCHANTMENT_EFFECT_COMPONENT_TYPE},
 * 而客户端无法识别这些项, 最终导致客户端无法登录服务器. 最无语的是客户端实际上不会用到这些数据.
 *
 * @author Ciallo
 */
@Mixin(RegistryDataLoader.RegistryData.class)
public class MixinRegistryDataLoader$RegistryData {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;Z)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static <T> Codec<T> koish$replaceEnchantmentCodec(Codec<T> codec, ResourceKey<? extends Registry<T>> key) {
        if (Registries.ENCHANTMENT.equals(key)) {
            Codec<Enchantment> sanitizedCodec = RecordCodecBuilder.create(
                    instance -> instance.group(
                            ComponentSerialization.CODEC.fieldOf("description").forGetter(Enchantment::description),
                            Enchantment.EnchantmentDefinition.CODEC.forGetter(Enchantment::definition),
                            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set", HolderSet.direct()).forGetter(Enchantment::exclusiveSet),
                            EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", DataComponentMap.EMPTY).forGetter(enchantment -> DataComponentMap.EMPTY)
                    ).apply(instance, Enchantment::new)
            );
            @SuppressWarnings("unchecked")
            Codec<T> replaced = (Codec<T>) sanitizedCodec;
            return replaced;
        }
        return codec;
    }
}

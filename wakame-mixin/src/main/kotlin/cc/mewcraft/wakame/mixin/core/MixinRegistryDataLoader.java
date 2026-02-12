package cc.mewcraft.wakame.mixin.core;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * 修改 {@link RegistryDataLoader#SYNCHRONIZED_REGISTRIES} 中的 {@link Enchantment#DIRECT_CODEC}
 * 使得在向客户端发送 {@link Enchantment} 时, 始终把 {@link Enchantment#effects()}
 * 当成是 {@link DataComponentMap#EMPTY}.
 * <p>
 * 这是因为我们添加了新的项到 {@link Registries#ENCHANTMENT_EFFECT_COMPONENT_TYPE},
 * 而客户端无法识别这些项, 最终导致客户端无法登录服务器. 最无语的是客户端实际上不会用到这些数据.
 *
 * @author Ciallo
 */
@Mixin(RegistryDataLoader.class)
public class MixinRegistryDataLoader {

    @Shadow
    public static final List<RegistryDataLoader.RegistryData<?>> SYNCHRONIZED_REGISTRIES = List.of(
            InvokerRegistryData.create(Registries.BIOME, Biome.NETWORK_CODEC),
            InvokerRegistryData.create(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.WOLF_VARIANT, WolfVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.WOLF_SOUND_VARIANT, WolfSoundVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.PIG_VARIANT, PigVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.FROG_VARIANT, FrogVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.CAT_VARIANT, CatVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.COW_VARIANT, CowVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.CHICKEN_VARIANT, ChickenVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.NETWORK_CODEC, true),
            InvokerRegistryData.create(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true),
            InvokerRegistryData.create(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC),
            // Koish start - 替换 Codec
            InvokerRegistryData.create(Registries.ENCHANTMENT, RecordCodecBuilder.create(
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
            )),
            // Koish end - 替换 Codec
            InvokerRegistryData.create(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.INSTRUMENT, Instrument.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.TEST_INSTANCE, GameTestInstance.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.DIALOG, Dialog.DIRECT_CODEC),
            InvokerRegistryData.create(Registries.TIMELINE, Timeline.NETWORK_CODEC)
    );
}

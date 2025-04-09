package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.EntityTypeWrapper;
import com.mojang.serialization.Codec;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobSpawnSettings.SpawnerData.class)
public abstract class MixinSpawnerData {
    @Redirect(
            method = "lambda$static$3",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/DefaultedRegistry;byNameCodec()Lcom/mojang/serialization/Codec;"
            )
    )
    private static Codec<EntityType<?>> wakame$byNameCodec(
            DefaultedRegistry<EntityType<?>> registry
    ) {
        return EntityTypeWrapper.CODEC;
    }
}

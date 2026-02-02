package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.KoishDataSanitizer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.item.ItemStack$2") // ItemStack#createOptionalStreamCodec
public abstract class MixinItemStack$2 {

    @Redirect(
            method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V",
                    ordinal = 1
            )
    )
    private void encode(StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> instance, Object buf, Object patch) {
        instance.encode((RegistryFriendlyByteBuf) buf, KoishDataSanitizer.sanitizeDataComponentPatch((DataComponentPatch) patch));
    }
}

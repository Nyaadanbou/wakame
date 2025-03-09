package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.DataComponentsPatch;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.item.ItemStack$1") // ItemStack#OPTIONAL_STREAM_CODEC
public abstract class MixinItemStackOptionalStreamCodec {

    @Redirect(
            method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V",
                    ordinal = 1
            )
    )
    private void encode(StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> instance, Object buf, Object patch) {
        instance.encode((RegistryFriendlyByteBuf) buf, ((DataComponentPatch) patch).forget(DataComponentsPatch::isCustomType));
    }

}

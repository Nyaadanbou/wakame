package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
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
        // 发包时不发送 Koish 添加的 DataComponent
        // 注: 其实可以改发包实现, 但 Mixin 性能更好
        var optional = ((DataComponentPatch) patch).get(ExtraDataComponents.DATA_CONTAINER);
        //noinspection OptionalAssignedToNull
        if (optional != null) { // 这里不为 null 的语义是该组件可能为 add 也可能为 remove - 只有这种情况需要 forget
            instance.encode((RegistryFriendlyByteBuf) buf, ((DataComponentPatch) patch).forget(ExtraDataComponents::isCustomType));
        } else {
            instance.encode((RegistryFriendlyByteBuf) buf, ((DataComponentPatch) patch));
        }
    }
}

package cc.mewcraft.wakame.mixin.core;

import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {

    /**
     * 注入玩家左键交互事件的所有触发点(下同, 3个函数对应3个触发点).
     * 使得左键交互事件只在玩家发送主手swing包时触发.
     * 仅使用原版客户端不可能发送副手swing包, 该注入对原版玩家无影响.
     * 在未进行此注入前, 若玩家发送了副手swing包, 会很奇怪的触发主手左键交互事件.
     */
    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void callEventOnlyWhenMainHand1(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (packet.getHand() != InteractionHand.MAIN_HAND) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    ordinal = 1
            ),
            cancellable = true
    )
    private void callEventOnlyWhenMainHand3(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (packet.getHand() != InteractionHand.MAIN_HAND) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;"
            ),
            cancellable = true
    )
    private void callEventOnlyWhenMainHand2(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (packet.getHand() != InteractionHand.MAIN_HAND) {
            ci.cancel();
        }
    }
}

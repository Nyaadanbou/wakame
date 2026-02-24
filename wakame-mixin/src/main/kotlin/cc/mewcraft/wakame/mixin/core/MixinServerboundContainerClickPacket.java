package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ContainerSyncSession;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerboundContainerClickPacket.class)
public class MixinServerboundContainerClickPacket {

    @WrapOperation(
            method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ServerGamePacketListener;handleContainerClick(Lnet/minecraft/network/protocol/game/ServerboundContainerClickPacket;)V"
            )
    )
    private void wrapHandleContainerClick(ServerGamePacketListener listener, ServerboundContainerClickPacket packet, Operation<Void> original) {
        if (listener instanceof ServerGamePacketListenerImpl listenerImpl) {
            try {
                ContainerSyncSession.INSTANCE.setPlayer(listenerImpl.getCraftPlayer());
                original.call(listener, packet);
            } finally {
                ContainerSyncSession.INSTANCE.unsetPlayer();
            }
        } else {
            original.call(listener, packet);
        }
    }
}

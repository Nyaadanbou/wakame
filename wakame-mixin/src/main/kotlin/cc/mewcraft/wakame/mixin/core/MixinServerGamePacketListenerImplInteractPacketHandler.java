package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$3")
public abstract class MixinServerGamePacketListenerImplInteractPacketHandler {
    @Final
    @Shadow
    ServerGamePacketListenerImpl this$0;

    /**
     * 服务端接收到来自客户端的interact包.
     * 注入点上文判定玩家可以攻击到实体.
     * 玩家正在左键点击生物, 触发左键事件.
     */
    @Inject(
            method = "onAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;attack(Lnet/minecraft/world/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onAttack(CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.isEmpty())
            return;
        PlayerItemLeftClickEvent leftClickEvent = new PlayerItemLeftClickEvent(this$0.player.getBukkitEntity(), itemStack.asBukkitMirror());
        leftClickEvent.callEvent();
    }
}

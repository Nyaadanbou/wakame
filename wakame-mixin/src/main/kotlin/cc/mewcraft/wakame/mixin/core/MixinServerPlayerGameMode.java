package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Final
    @Shadow
    protected ServerPlayer player;

    /**
     * 服务端接收到来自客户端的player action包.
     * 玩家开始挖掘方块, 触发左键事件.
     * <p>
     * 此处玩家左键方块不用swing包处理的原因:
     * 玩家生存模式挖掘方块时会持续发送swing包.
     * 此时用只发送一次的player action包判定更加准确.
     */
    @Inject(
            method = "handleBlockBreakAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void onLeftClickBlockSurvival(CallbackInfo ci) {
        ItemStack selected = this.player.getInventory().getSelected();
        if (selected.isEmpty())
            return;
        PlayerItemLeftClickEvent leftClickEvent = new PlayerItemLeftClickEvent(this.player.getBukkitEntity(), selected.asBukkitMirror());
        leftClickEvent.callEvent();
    }

//    @Inject(
//            method = "useItemOn",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;ZZLnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lorg/bukkit/event/player/PlayerInteractEvent;",
//                    shift = At.Shift.AFTER
//            )
//    )
//    private void onRightClickBlock(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
//        if (stack.isEmpty())
//            return;
//        PlayerItemRightClickEvent rightClickEvent = new PlayerItemRightClickEvent(player.getBukkitEntity(), stack.asBukkitMirror(), hand);
//        rightClickEvent.callEvent();
//    }

}

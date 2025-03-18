package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent;
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Final
    @Shadow
    protected ServerPlayer player;

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

    @Inject(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;ZZLnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void onRightClickBlock(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (stack.isEmpty())
            return;
        PlayerItemRightClickEvent rightClickEvent = new PlayerItemRightClickEvent(player.getBukkitEntity(), stack.asBukkitMirror(), hand);
        rightClickEvent.callEvent();
    }

}

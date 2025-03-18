package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatItem.class)
public abstract class MixinBoatItem {
    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void onRightClickBlock(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local ItemStack itemStack) {
        if (itemStack.isEmpty())
            return;
        if (!(user instanceof ServerPlayer serverPlayer))
            return;
        PlayerItemRightClickEvent rightClickEvent = new PlayerItemRightClickEvent(serverPlayer.getBukkitEntity(), itemStack.asBukkitMirror(), hand);
        rightClickEvent.callEvent();
    }
}

package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent;
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {

    @Shadow
    public ServerPlayer player;

    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    ordinal = 0,
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void onLeftClickAirSurvival(CallbackInfo ci) {
        ItemStack selected = this.player.getInventory().getSelected();
        if (selected.isEmpty())
            return;
        PlayerItemLeftClickEvent leftClickEvent = new PlayerItemLeftClickEvent(this.player.getBukkitEntity(), selected.asBukkitMirror());
        leftClickEvent.callEvent();
    }

    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void onLeftClickAdventure(CallbackInfo ci) {
        PlayerItemLeftClickEvent leftClickEvent = new PlayerItemLeftClickEvent(this.player.getBukkitEntity(), this.player.getInventory().getSelected().asBukkitMirror());
        leftClickEvent.callEvent();
    }

    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayerGameMode;getGameModeForPlayer()Lnet/minecraft/world/level/GameType;",
                    shift = At.Shift.AFTER
            )
    )
    private void afterLeftClickRaytrace(CallbackInfo ci, @Local Location origin, @Local RayTraceResult result) {
        if (result != null) {
            GameType gameType = this.player.gameMode.getGameModeForPlayer();
            if (gameType != GameType.CREATIVE && result.getHitEntity() != null && origin.toVector().distanceSquared(result.getHitPosition()) < this.player.entityInteractionRange() * this.player.entityInteractionRange()) {
                ItemStack selected = this.player.getInventory().getSelected();
                if (selected.isEmpty())
                    return;
                PlayerItemLeftClickEvent leftClickEvent = new PlayerItemLeftClickEvent(this.player.getBukkitEntity(), selected.asBukkitMirror());
                leftClickEvent.callEvent();
            }
        }
    }

    @Inject(
            method = "handleUseItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void onRightClickAirSurvival(CallbackInfo ci, @Local(ordinal = 0) InteractionHand enumhand) {
        PlayerItemRightClickEvent rightClickEvent = new PlayerItemRightClickEvent(this.player.getBukkitEntity(), this.player.getInventory().getSelected().asBukkitMirror(), enumhand);
        rightClickEvent.callEvent();
    }

    @Inject(
            method = "handlePlayerAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;getAction()Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;",
                    shift = At.Shift.AFTER
            )
    )
    private void onHandlePlayerAction(CallbackInfo ci, @Local(argsOnly = true) ServerboundPlayerActionPacket packet) {
        if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            ItemStack selected = this.player.getInventory().getSelected();
            if (selected.isEmpty())
                return;
            PlayerItemLeftClickEvent leftClickEvent = new PlayerItemLeftClickEvent(this.player.getBukkitEntity(), selected.asBukkitMirror());
            leftClickEvent.callEvent();
        }
    }
}

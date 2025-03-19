package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent;
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {

    @Shadow
    public ServerPlayer player;

    /**
     * 服务端接收到来自客户端的swing包.
     * 注入点上文玩家射线检测的结果为null/碰撞到实体交互距离以外的实体.
     * 判定玩家正在左键点击空气, 触发左键事件.
     */
    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
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

    /**
     * 服务端接收到来自客户端的swing包.
     * 注入点上文玩家射线检测结果为"与方块碰撞".
     * 注入点上文玩家喂冒险模式.
     * 玩家正在左键点击方块, 触发左键事件.
     * <p>
     * 此处玩家左键方块用swing包处理的原因:
     * 冒险模式玩家左键点击无法破坏的方块不会发player action包.
     * 只能通过swing包检测.
     * <p>
     * 注意:
     * 使用特殊组件的物品使冒险模式的玩家能够破坏方块, 会导致左键事件触发两次.
     * 此问题无法解决, paper如是说.
     */
    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lorg/bukkit/event/player/PlayerInteractEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void onLeftClickAdventure(CallbackInfo ci) {
        ItemStack selected = this.player.getInventory().getSelected();
        if (selected.isEmpty())
            return;
        PlayerItemLeftClickEvent leftClickEvent = new PlayerItemLeftClickEvent(this.player.getBukkitEntity(), selected.asBukkitMirror());
        leftClickEvent.callEvent();
    }

    /**
     * 客户端发送了use item包, 触发右键事件.
     */
    @Inject(
            method = "handleUseItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;",
                    shift = At.Shift.AFTER
            )
    )
    private void onRightClick(CallbackInfo ci, @Local(ordinal = 0) InteractionHand enumhand, @Local(ordinal = 0) ItemStack itemstack) {
        PlayerItemRightClickEvent rightClickEvent = new PlayerItemRightClickEvent(this.player.getBukkitEntity(), itemstack.asBukkitMirror(), enumhand);
        rightClickEvent.callEvent();
    }

//    /**
//     * 客户端发送了use item包, 触发右键事件.
//     */
//    @Inject(
//            method = "handleUseItem",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lorg/bukkit/craftbukkit/event/CraftEventFactory;callPlayerInteractEvent(Lnet/minecraft/world/entity/player/Player;Lorg/bukkit/event/block/Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lorg/bukkit/event/player/PlayerInteractEvent;",
//                    shift = At.Shift.AFTER
//            )
//    )
//    private void onRightClickBlock(CallbackInfo ci, @Local(ordinal = 0) InteractionHand enumhand, @Local(ordinal = 0) ItemStack itemstack) {
//        if (itemstack.isEmpty())
//            return;
//        PlayerItemRightClickEvent rightClickEvent = new PlayerItemRightClickEvent(this.player.getBukkitEntity(), itemstack.asBukkitMirror(), enumhand);
//        rightClickEvent.callEvent();
//    }
}

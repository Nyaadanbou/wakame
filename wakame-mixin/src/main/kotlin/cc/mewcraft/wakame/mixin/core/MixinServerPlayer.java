package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraCriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/level/ServerPlayer$2")
public class MixinServerPlayer {

    @Final
    @Shadow
    ServerPlayer this$0; // 以这种方式获取 net/minecraft/server/level/ServerPlayer 的实例

    /**
     * 在 `minecraft:inventory_changed` 之后额外触发 `koish:inventory_changed`.
     */
    @Inject(
            method = "slotChanged(Lnet/minecraft/world/inventory/AbstractContainerMenu;ILnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/criterion/InventoryChangeTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void injected0(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack, CallbackInfo ci) {
        ExtraCriteriaTriggers.INVENTORY_CHANGED.trigger(this$0, this$0.getInventory(), stack);
    }

    /**
     * 在 `minecraft:inventory_changed` 之后额外触发 `koish:inventory_changed`.
     */
    @Inject(
            method = "slotChanged(Lnet/minecraft/world/inventory/AbstractContainerMenu;ILnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/criterion/InventoryChangeTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void injected1(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack oldStack, ItemStack stack, CallbackInfo ci) {
        ExtraCriteriaTriggers.INVENTORY_CHANGED.trigger(this$0, this$0.getInventory(), stack);
    }
}

package cc.mewcraft.wakame.mixin.support;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CustomContainerListener implements ContainerListener {
    private final ServerPlayer serverPlayer;

    public CustomContainerListener(ServerPlayer player) {
        this.serverPlayer = player;
    }

    @Override
    public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
        Slot slot = handler.getSlot(slotId);

        if (!(slot instanceof ResultSlot)) {
            if (slot.container == this.serverPlayer.getInventory()) {
                CriteriaTriggers.INVENTORY_CHANGED.trigger(this.serverPlayer, this.serverPlayer.getInventory(), stack);
            }
        }
    }

    // Paper start - Add PlayerInventorySlotChangeEvent
    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack oldStack, ItemStack stack) {
        Slot slot = containerToSend.getSlot(dataSlotIndex);
        if (!(slot instanceof ResultSlot)) {
            if (slot.container == this.serverPlayer.getInventory()) {
                if (PlayerInventorySlotChangeEvent.getHandlerList().getRegisteredListeners().length == 0) {
                    CriteriaTriggers.INVENTORY_CHANGED.trigger(this.serverPlayer, this.serverPlayer.getInventory(), stack);
                    return;
                }
                // Wakame start - pass ItemStack as mirror
                PlayerInventorySlotChangeEvent event = new PlayerInventorySlotChangeEvent(
                        serverPlayer.getBukkitEntity(),
                        dataSlotIndex,
                        CraftItemStack.asCraftMirror(oldStack),
                        CraftItemStack.asCraftMirror(stack)
                );
                event.callEvent();
                // Wakame end - pass ItemStack as mirror
                if (event.shouldTriggerAdvancements()) {
                    CriteriaTriggers.INVENTORY_CHANGED.trigger(this.serverPlayer, this.serverPlayer.getInventory(), stack);
                }
            }
        }
    }
    // Paper end - Add PlayerInventorySlotChangeEvent

    @Override
    public void dataChanged(AbstractContainerMenu handler, int property, int value) {
    }
}
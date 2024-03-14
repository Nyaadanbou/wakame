package cc.mewcraft.wakame.transformer;

import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InventoryListenerProxy implements ContainerListener {
    private final ServerPlayer serverPlayer;
    private final ContainerListener originalListener;

    public InventoryListenerProxy(ServerPlayer serverPlayer, ContainerListener originalListener) {
        this.serverPlayer = serverPlayer;
        this.originalListener = originalListener;
    }

    @Override
    public void slotChanged(@NotNull AbstractContainerMenu handler, int slotId, @NotNull ItemStack stack) {
        originalListener.slotChanged(handler, slotId, stack);
    }

    @Override
    public void slotChanged(@NotNull AbstractContainerMenu handler, int slotId, @NotNull ItemStack oldStack, @NotNull ItemStack stack) {
        // injected code - start
        new PlayerInventorySlotChangeEvent(
                serverPlayer.getBukkitEntity(),
                slotId,
                oldStack.asBukkitMirror(),
                stack.asBukkitMirror()
        ).callEvent();
        // injected code - end
        originalListener.slotChanged(handler, slotId, oldStack, stack);
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu handler, int property, int value) {
        originalListener.dataChanged(handler, property, value);
    }
}

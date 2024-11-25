package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.item.ItemSlot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a slot contents change in a player's inventory.
 */
public class PlayerItemSlotChangeEvent extends PlayerEvent {
    public static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final ItemSlot slot;
    private final ItemStack oldItemStack;
    private final ItemStack newItemStack;

    public PlayerItemSlotChangeEvent(
            @NotNull Player player,
            @NotNull ItemSlot slot,
            @Nullable ItemStack oldItemStack,
            @Nullable ItemStack newItemStack
    ) {
        super(player);
        this.slot = slot;
        this.oldItemStack = oldItemStack;
        this.newItemStack = newItemStack;
    }

    /**
     * Gets the {@link #slot}.
     *
     * @return the {@link #slot}.
     */
    public @NotNull ItemSlot getSlot() {
        return slot;
    }

    /**
     * Mirror of ItemStack that was in the slot before the change.
     *
     * @return The old ItemStack in the slot.
     */
    public @Nullable ItemStack getOldItemStack() {
        return oldItemStack;
    }

    /**
     * Mirror of ItemStack that is in the slot after the change.
     *
     * @return The new ItemStack in the slot.
     */
    public @Nullable ItemStack getNewItemStack() {
        return newItemStack;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}

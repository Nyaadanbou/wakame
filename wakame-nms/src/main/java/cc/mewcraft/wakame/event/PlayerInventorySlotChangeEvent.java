package cc.mewcraft.wakame.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a slot contents change in a player's inventory.
 * <p>
 * This is a mimic of {@code PlayerInventorySlotChangeEvent} with the difference
 * in that the {@link #getOldItemStack()} and {@link #getNewItemStack()} return a Bukkit
 * mirror of NMS item, instead of a Bukkit copy. This should have much better
 * performance since there is no deep clone.
 *
 * @see io.papermc.paper.event.player.PlayerInventorySlotChangeEvent
 */
public class PlayerInventorySlotChangeEvent extends PlayerEvent {
    public static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final int rawSlot;
    private final int slot;
    private final ItemStack oldItemStack;
    private final ItemStack newItemStack;

    public PlayerInventorySlotChangeEvent(Player player, int rawSlot, ItemStack oldItemStack, ItemStack newItemStack) {
        super(player);
        this.rawSlot = rawSlot;
        this.slot = player.getOpenInventory().convertSlot(rawSlot);
        this.oldItemStack = oldItemStack;
        this.newItemStack = newItemStack;
    }

    /**
     * The raw slot number that was changed.
     *
     * @return The raw slot number.
     */
    public int getRawSlot() {
        return rawSlot;
    }

    /**
     * The slot number that was changed, ready for passing to
     * {@link Inventory#getItem(int)}. Note that there may be two slots with
     * the same slot number, since a view links two different inventories.
     * <p>
     * If no inventory is opened, internal crafting view is used for conversion.
     *
     * @return The slot number.
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Clone of ItemStack that was in the slot before the change.
     *
     * @return The old ItemStack in the slot.
     */
    public @NotNull ItemStack getOldItemStack() {
        return oldItemStack;
    }

    /**
     * Clone of ItemStack that is in the slot after the change.
     *
     * @return The new ItemStack in the slot.
     */
    public @NotNull ItemStack getNewItemStack() {
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

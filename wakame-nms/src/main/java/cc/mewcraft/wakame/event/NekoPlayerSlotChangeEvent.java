package cc.mewcraft.wakame.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NekoPlayerSlotChangeEvent extends Event {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int slot;
    private final ItemStack oldItem;
    private final ItemStack newItem;

    public NekoPlayerSlotChangeEvent(Player player, int slot, ItemStack oldItem, ItemStack newItem) {
        this.player = player;
        this.slot = slot;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }
    @NotNull
    public Player getPlayer() {
        return player;
    }

    public int getSlot() {
        return slot;
    }
    @NotNull
    public ItemStack getOldItem() {
        return oldItem;
    }
    @NotNull
    public ItemStack getNewItem() {
        return newItem;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}

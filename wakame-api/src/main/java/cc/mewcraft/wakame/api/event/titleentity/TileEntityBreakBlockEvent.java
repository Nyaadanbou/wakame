package cc.mewcraft.wakame.api.event.titleentity;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when a {@link TileEntity} breaks a block.
 */
public class TileEntityBreakBlockEvent extends TileEntityEvent {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final @NotNull Block block;
    private final @NotNull List<@NotNull ItemStack> drops;

    public TileEntityBreakBlockEvent(@NotNull TileEntity tileEntity, @NotNull Block block, @NotNull List<ItemStack> drops) {
        super(tileEntity);
        this.block = block;
        this.drops = drops;
    }

    public @NotNull Block getBlock() {
        return block;
    }

    public @NotNull List<@NotNull ItemStack> getDrops() {
        return drops;
    }

    @Override public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override public @NotNull String toString() {
        return "TileEntityBreakBlockEvent{" + "block=" + block + ", drops=" + drops + ", tileEntity=" + getTileEntity() + '}';
    }

}
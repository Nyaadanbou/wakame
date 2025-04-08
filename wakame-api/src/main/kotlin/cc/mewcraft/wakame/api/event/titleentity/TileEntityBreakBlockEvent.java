package cc.mewcraft.wakame.api.event.titleentity;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Called when a {@link TileEntity} breaks a block.
 */
@NullMarked
public class TileEntityBreakBlockEvent extends TileEntityEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Block block;
    private final List<ItemStack> drops;

    public TileEntityBreakBlockEvent(TileEntity tileEntity, Block block, List<ItemStack> drops) {
        super(tileEntity);
        this.block = block;
        this.drops = drops;
    }

    public Block getBlock() {
        return block;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public String toString() {
        return "TileEntityBreakBlockEvent{" + "block=" + block + ", drops=" + drops + ", tileEntity=" + getTileEntity() + '}';
    }

}
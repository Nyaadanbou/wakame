package cc.mewcraft.wakame.api.protection;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
@NullMarked
public interface ProtectionIntegration {

    /**
     * Specifies from which thread methods in this protection integration are allowed to be called.
     *
     * @return the {@link ExecutionMode}
     */
    default ExecutionMode getExecutionMode() {
        // 开发日记 2024/11/25 小米
        // 也许我们服务器根本不需要异步的区域检查?
        // 因为一般来说, 区域检查都是玩家主动触发的, 那么自然而然就在主线程上.
        // 其他情况, 由非玩家发起的区域检查 (比如持续运行的机器) 可能会在其他线程上?
        return ExecutionMode.SERVER;
    }

    /**
     * Checks if that {@link OfflinePlayer} can break a block at that {@link Location} using that {@link ItemStack}.
     *
     * @param player   the player trying to break the block
     * @param item     the item to break the block with
     * @param location the location of the block
     * @return if the player can break the block
     */
    boolean canBreak(OfflinePlayer player, @Nullable ItemStack item, Location location);

    /**
     * Checks if that {@link TileEntity} can break a block at that {@link Location} using that {@link ItemStack}.
     *
     * @param tileEntity the tile-entity trying to break the block
     * @param item       the item to break the block with
     * @param location   the location of the block
     * @return if the tile-entity can break the block
     */
    default boolean canBreak(TileEntity tileEntity, @Nullable ItemStack item, Location location) {
        return canBreak(requireNonNull(tileEntity.getOwner()), item, location);
    }

    /**
     * Checks if that {@link OfflinePlayer} can place an {@link ItemStack} at that {@link Location}.
     *
     * @param player   the player trying to place the block
     * @param item     the item to place
     * @param location the location of the block
     * @return if the player can place the block
     */
    boolean canPlace(OfflinePlayer player, ItemStack item, Location location);

    /**
     * Checks if that {@link TileEntity} can place an {@link ItemStack} at that {@link Location}.
     *
     * @param tileEntity the tile-entity trying to place the block
     * @param item       the item to place
     * @param location   the location of the block
     * @return if the tile-entity can place the block
     */
    default boolean canPlace(TileEntity tileEntity, ItemStack item, Location location) {
        return canPlace(requireNonNull(tileEntity.getOwner()), item, location);
    }

    /**
     * Checks if the {@link OfflinePlayer} can interact with a block at that {@link Location} using that {@link ItemStack}.
     *
     * @param player   the player trying to interact with the block
     * @param item     the item used to interact with the block
     * @param location the location of the block
     * @return if the player can interact with the block
     */
    boolean canUseBlock(OfflinePlayer player, @Nullable ItemStack item, Location location);

    /**
     * Checks if the {@link TileEntity} can interact with a block at that {@link Location} using that {@link ItemStack}.
     *
     * @param tileEntity the tile-entity trying to interact with the block
     * @param item       the item used to interact with the block
     * @param location   the location of the block
     * @return if the tile-entity can interact with the block
     */
    default boolean canUseBlock(TileEntity tileEntity, @Nullable ItemStack item, Location location) {
        return canUseBlock(requireNonNull(tileEntity.getOwner()), item, location);
    }

    /**
     * Checks if the {@link OfflinePlayer} can use that {@link ItemStack} at that {@link Location}.
     *
     * @param player   the player trying to use the item
     * @param item     the item the player tries to use
     * @param location the location of the player
     * @return if the player can use the item
     */
    boolean canUseItem(OfflinePlayer player, ItemStack item, Location location);

    /**
     * Checks if the {@link TileEntity} can use that {@link ItemStack} at that {@link Location}.
     *
     * @param tileEntity the tile-entity trying to use the item
     * @param item       the item the player tries to use
     * @param location   the location of the player
     * @return if the tile-entity can use the item
     */
    default boolean canUseItem(TileEntity tileEntity, ItemStack item, Location location) {
        return canUseItem(requireNonNull(tileEntity.getOwner()), item, location);
    }

    /**
     * Checks if the {@link OfflinePlayer} can interact with the {@link Entity} using the {@link ItemStack}.
     *
     * @param player the player trying to interact with the entity
     * @param entity the entity the player is trying to interact with
     * @param item   the item the player is holding in their hand
     * @return if the player can interact with the entity
     */
    boolean canInteractWithEntity(OfflinePlayer player, Entity entity, @Nullable ItemStack item);

    /**
     * Checks if the {@link TileEntity} can interact with the {@link Entity} using the {@link ItemStack}.
     *
     * @param tileEntity the tile-entity trying to interact with the entity
     * @param entity     the entity the player is trying to interact with
     * @param item       the item the player is holding in their hand
     * @return if the tile-entity can interact with the entity
     */
    default boolean canInteractWithEntity(TileEntity tileEntity, Entity entity, @Nullable ItemStack item) {
        return canInteractWithEntity(requireNonNull(tileEntity.getOwner()), entity, item);
    }

    /**
     * Checks if the {@link OfflinePlayer} can hurt the {@link Entity} with this {@link ItemStack}.
     *
     * @param player the player trying to hurt the entity
     * @param entity the entity the player is trying to hurt
     * @param item   the item the player is holding in their hand
     * @return if the player can hurt the entity
     */
    boolean canHurtEntity(OfflinePlayer player, Entity entity, @Nullable ItemStack item);

    /**
     * Checks if the {@link TileEntity} can hurt the {@link Entity} with this {@link ItemStack}.
     *
     * @param tileEntity the tile-entity trying to hurt the entity
     * @param entity     the entity the player is trying to hurt
     * @param item       the item the player is holding in their hand
     * @return if the tile-entity can hurt the entity
     */
    default boolean canHurtEntity(TileEntity tileEntity, Entity entity, @Nullable ItemStack item) {
        return canHurtEntity(requireNonNull(tileEntity.getOwner()), entity, item);
    }

    /**
     * Defines how methods in this protection integration are allowed to be called.
     */
    enum ExecutionMode {

        /**
         * The method can be called from any thread.
         */
        NONE,

        /**
         * The methods are always called from the server thread.
         */
        SERVER,

    }

}
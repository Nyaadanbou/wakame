package cc.mewcraft.wakame.api;

import cc.mewcraft.wakame.api.block.BlockManager;
import cc.mewcraft.wakame.api.block.NekoBlockRegistry;
import cc.mewcraft.wakame.api.item.NekoItemRegistry;
import cc.mewcraft.wakame.api.protection.ProtectionIntegration;
import cc.mewcraft.wakame.api.tileentity.TileEntityManager;
import org.jetbrains.annotations.NotNull;

/**
 * The Nekoo API.
 * <p>
 * Use {@link NekooProvider} to get the instance of this class.
 */
public interface Nekoo {
    /**
     * Gets the {@link TileEntityManager}.
     *
     * @return The {@link TileEntityManager}.
     */
    TileEntityManager getTileEntityManager();

    /**
     * Gets the {@link BlockManager}.
     *
     * @return The {@link BlockManager}.
     */
    BlockManager getBlockManager();

    /**
     * Gets the {@link NekoBlockRegistry}.
     *
     * @return The {@link NekoBlockRegistry}.
     */
    NekoBlockRegistry getBlockRegistry();

    /**
     * Gets the {@link NekoItemRegistry}.
     *
     * @return The {@link NekoItemRegistry}.
     */
    NekoItemRegistry getItemRegistry();

    /**
     * Registers a {@link ProtectionIntegration}.
     *
     * @param integration The {@link ProtectionIntegration} to register.
     */
    void registerProtectionIntegration(@NotNull ProtectionIntegration integration);
}
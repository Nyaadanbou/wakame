package cc.mewcraft.wakame.api;

import cc.mewcraft.wakame.api.block.BlockManager;
import cc.mewcraft.wakame.api.block.KoishBlockRegistry;
import cc.mewcraft.wakame.api.item.KoishItemRegistry;
import cc.mewcraft.wakame.api.protection.ProtectionIntegration;
import cc.mewcraft.wakame.api.tileentity.TileEntityManager;
import org.jetbrains.annotations.NotNull;

/**
 * The Koish API.
 * <p>
 * Use {@link KoishProvider} to get the instance of this class.
 */
public interface Koish {

    /**
     * Gets the Koish instance.
     *
     * @return The Koish instance.
     * @throws IllegalStateException If Koish is not installed on this server.
     */
    static Koish get() {
        return KoishProvider.get();
    }

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
     * Gets the {@link KoishBlockRegistry}.
     *
     * @return The {@link KoishBlockRegistry}.
     */
    KoishBlockRegistry getBlockRegistry();

    /**
     * Gets the {@link KoishItemRegistry}.
     *
     * @return The {@link KoishItemRegistry}.
     */
    KoishItemRegistry getItemRegistry();

    /**
     * Registers a {@link ProtectionIntegration}.
     *
     * @param integration The {@link ProtectionIntegration} to register.
     */
    void registerProtectionIntegration(@NotNull ProtectionIntegration integration);
}
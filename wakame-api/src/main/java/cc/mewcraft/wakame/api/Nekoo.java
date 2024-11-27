package cc.mewcraft.wakame.api;

import cc.mewcraft.wakame.api.block.BlockManager;
import cc.mewcraft.wakame.api.block.NekoBlockRegistry;
import cc.mewcraft.wakame.api.item.NekoItemRegistry;
import cc.mewcraft.wakame.api.protection.ProtectionIntegration;
import cc.mewcraft.wakame.api.tileentity.TileEntityManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    /**
     * 通过指定萌芽物品的 {@link Key} 创建一个新的 {@link ItemStack}.
     * <p>
     * 你可以传入一个 {@link Player} source, 使得生成结果基于该玩家的信息.
     */
    @Deprecated
    ItemStack createItemStack(Key id, Player source);

    /**
     * @see #createItemStack(Key, Player)
     */
    @Deprecated
    ItemStack createItemStack(String namespace, String path, Player source);

    /**
     * 判断 {@link ItemStack} 是否为萌芽物品.
     */
    @Deprecated
    boolean isNekoStack(ItemStack itemStack);

    /**
     * 如果 {@link ItemStack} 是萌芽物品, 则返回对应萌芽物品的唯一标识.
     * 如果 {@link ItemStack} 不是萌芽物品, 则返回 null.
     */
    @Deprecated
    Key getNekoItemId(ItemStack itemStack);
}
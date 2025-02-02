// 开发日记 2024/11/24 小米
// 目前我们没有计划实现真正意义上的 TileEntity (真实的 Block 附带 tick 逻辑 + 交互逻辑),
// 但是会实现类似 TileEntity 的游戏机制, 将采用屏障方块 + 展示实体 + 交互实体的方式实现.
//
// [屏障方块] 相当于方块系统中的唯一的 Block 底层对象, 用于创造碰撞体积, 以及实现最基本的方块机制 - 在空间上占个位置.
// [展示实体] 则可以让我们非常简单的实现每秒 tick 和玩家的交互逻辑, 具体来说:
// - 因为是实体, 所以可以很方便的遍历它们, 这让编写每 tick 逻辑或者是玩家交互逻辑都很直接.
// - 展示实体的特性允许我们完全自定义这个方块的样子, 并且自由度远超 MC 中普通的方块模型.

package cc.mewcraft.wakame.api.tileentity;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
@NullMarked
public interface TileEntityManager {

    /**
     * Gets the {@link TileEntity} at that {@link Location} or null if there isn't one.
     *
     * @return the {@link TileEntity} at that {@link Location} or null if there isn't one
     */
    @Nullable
    TileEntity getTileEntity(Location location);

    /**
     * Gets all loaded {@link TileEntity TileEntities} in the specified {@link Chunk}.
     *
     * @param chunk the {@link Chunk} to get the {@link TileEntity TileEntities} from
     * @return all loaded {@link TileEntity TileEntities} in the specified {@link Chunk}
     */
    List<TileEntity> getTileEntities(Chunk chunk);

    /**
     * Gets all loaded {@link TileEntity TileEntities} in the specified {@link World}.
     *
     * @param world the {@link World} to get the {@link TileEntity TileEntities} from
     * @return all loaded {@link TileEntity TileEntities} in the specified {@link World}
     */
    List<TileEntity> getTileEntities(World world);

    /**
     * Gets all loaded {@link TileEntity TileEntities}.
     *
     * @return all loaded {@link TileEntity TileEntities}
     */
    List<TileEntity> getTileEntities();

}
package cc.mewcraft.wakame.integration.protection

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.tileentity.TileEntity
import cc.mewcraft.wakame.context.Context
import cc.mewcraft.wakame.context.intention.DefaultContextIntentions.BlockBreak
import cc.mewcraft.wakame.context.intention.DefaultContextIntentions.BlockInteract
import cc.mewcraft.wakame.context.intention.DefaultContextIntentions.BlockPlace
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.integration.HooksLoader
import cc.mewcraft.wakame.integration.permission.PermissionManager
import cc.mewcraft.wakame.world.BlockPos
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

//<editor-fold desc="ProtectionArgs classes", defaultstate="collapsed">
private interface ProtectionArgs {
    val player: OfflinePlayer
    val location: Location
}

private interface ProtectionArgsTileEntity : ProtectionArgs {
    val tileEntity: TileEntity
    override val player get() = tileEntity.owner!!
}

private data class CanPlaceUserArgs(override val player: OfflinePlayer, val item: ItemStack, override val location: Location) :
    ProtectionArgs

private data class CanPlaceTileArgs(override val tileEntity: TileEntity, val item: ItemStack, override val location: Location) :
    ProtectionArgsTileEntity

private data class CanBreakUserArgs(override val player: OfflinePlayer, val item: ItemStack?, override val location: Location) :
    ProtectionArgs

private data class CanBreakTileArgs(override val tileEntity: TileEntity, val item: ItemStack?, override val location: Location) :
    ProtectionArgsTileEntity

private data class CanUseBlockUserArgs(override val player: OfflinePlayer, val item: ItemStack?, override val location: Location) :
    ProtectionArgs

private data class CanUseBlockTileArgs(override val tileEntity: TileEntity, val item: ItemStack?, override val location: Location) :
    ProtectionArgsTileEntity

private data class CanUseItemUserArgs(override val player: OfflinePlayer, val item: ItemStack, override val location: Location) :
    ProtectionArgs

private data class CanUseItemTileArgs(override val tileEntity: TileEntity, val item: ItemStack, override val location: Location) :
    ProtectionArgsTileEntity

private data class CanInteractWithEntityUserArgs(override val player: OfflinePlayer, val entity: Entity, val item: ItemStack?) :
    ProtectionArgs {
    override val location: Location = entity.location
}

private data class CanInteractWithEntityTileArgs(override val tileEntity: TileEntity, val entity: Entity, val item: ItemStack?) :
    ProtectionArgsTileEntity {
    override val location: Location = entity.location
}

private data class CanHurtEntityUserArgs(override val player: OfflinePlayer, val entity: Entity, val item: ItemStack?) :
    ProtectionArgs {
    override val location: Location = entity.location
}

private data class CanHurtEntityTileArgs(override val tileEntity: TileEntity, val entity: Entity, val item: ItemStack?) :
    ProtectionArgsTileEntity {
    override val location: Location = entity.location
}
//</editor-fold>

/**
 * 通过已注册的 [ProtectionIntegration] 处理保护检查.
 *
 * 本实现不会对保护检查结果缓存, 缓存由底层的保护系统自己实现.
 * 如果服务器用的是 Towny/WorldGuard, 它们会缓存所有保护检查, 可以不必担心缓存问题.
 * 如果是其他保护插件, 请确保它们缓存了保护检查结果. 否则将会导致性能问题.
 */
@PostWorldDependency(
    runBefore = [HooksLoader::class, PermissionManager::class]
)
object ProtectionManager : Initializable {

    internal val integrations = ArrayList<ProtectionIntegration>()

    override fun onPostWorld() {
        init()
    }

    private fun init() {
        // no ops
    }

    /**
     * Checks whether the given [ctx] passes place permission checks.
     */
    fun canPlace(ctx: Context<BlockPlace>): Boolean {
        val pos = ctx.getOrThrow(DefaultContextParamTypes.BLOCK_POS)
        val blockItem = ctx[DefaultContextParamTypes.BLOCK_ITEM_STACK] ?: ItemStack(Material.AIR)

        val tileEntity = ctx[DefaultContextParamTypes.SOURCE_TILE_ENTITY]
        if (tileEntity != null) {
            return canPlace(tileEntity, blockItem, pos)
        }

        val responsiblePlayer = ctx[DefaultContextParamTypes.RESPONSIBLE_PLAYER]
        if (responsiblePlayer != null) {
            return canPlace(responsiblePlayer, blockItem, pos)
        }

        return true
    }

    /**
     * Checks if the [tileEntity] can place that [item] at that [pos].
     */
    fun canPlace(tileEntity: TileEntity, item: ItemStack, pos: BlockPos): Boolean {
        if (tileEntity.owner == null) {
            return true
        }
        return check0(CanPlaceTileArgs(tileEntity, item.clone(), pos.location)) { canPlace(tileEntity, item, pos.location) }
    }

    /**
     * Checks if the [player] can place that [item] at that [pos].
     */
    fun canPlace(player: OfflinePlayer, item: ItemStack, pos: BlockPos): Boolean {
        return check0(CanPlaceUserArgs(player, item.clone(), pos.location)) { canPlace(player, item, pos.location) }
    }


    /**
     * Checks if the [player] can place that [item] at that [pos].
     */
    fun canPlace(player: Player, item: ItemStack, pos: BlockPos): Boolean {
        return check0(CanPlaceUserArgs(player, item.clone(), pos.location)) { canPlace(player, item, pos.location) }
    }

    /**
     * Checks whether the given [ctx] passes break permission checks.
     */
    fun canBreak(ctx: Context<BlockBreak>): Boolean {
        val pos = ctx.getOrThrow(DefaultContextParamTypes.BLOCK_POS)
        val tool = ctx[DefaultContextParamTypes.TOOL_ITEM_STACK]

        val tileEntity = ctx[DefaultContextParamTypes.SOURCE_TILE_ENTITY]
        if (tileEntity != null) {
            return canBreak(tileEntity, tool, pos)
        }

        val responsiblePlayer = ctx[DefaultContextParamTypes.RESPONSIBLE_PLAYER]
        if (responsiblePlayer != null) {
            return canBreak(responsiblePlayer, tool, pos)
        }

        return true
    }

    /**
     * Checks if that [tileEntity] can break a block at that [pos] using that [item].
     */
    fun canBreak(tileEntity: TileEntity, item: ItemStack?, pos: BlockPos): Boolean {
        if (tileEntity.owner == null) {
            return true
        }
        return check0(CanBreakTileArgs(tileEntity, item?.clone(), pos.location)) { canBreak(tileEntity, item, pos.location) }
    }

    /**
     * Checks if that [player] can break a block at that [pos] using that [item].
     */
    fun canBreak(player: OfflinePlayer, item: ItemStack?, pos: BlockPos): Boolean {
        return check0(CanBreakUserArgs(player, item?.clone(), pos.location)) { canBreak(player, item, pos.location) }
    }

    /**
     * Checks if that [player] can break a block at that [pos] using that [item].
     */
    fun canBreak(player: Player, item: ItemStack?, pos: BlockPos): Boolean {
        return check0(CanBreakUserArgs(player, item?.clone(), pos.location)) { canBreak(player, item, pos.location) }
    }

    /**
     * Checks whether the given [ctx] passes block interaction permission checks.
     */
    fun canUseBlock(ctx: Context<BlockInteract>): Boolean {
        val pos = ctx.getOrThrow(DefaultContextParamTypes.BLOCK_POS)
        val item = ctx[DefaultContextParamTypes.INTERACTION_ITEM_STACK]

        val tileEntity = ctx[DefaultContextParamTypes.SOURCE_TILE_ENTITY]
        if (tileEntity != null) {
            return canUseBlock(tileEntity, item, pos)
        }

        val responsiblePlayer = ctx[DefaultContextParamTypes.RESPONSIBLE_PLAYER]
        if (responsiblePlayer != null) {
            return canUseBlock(responsiblePlayer, item, pos)
        }

        return true
    }

    /**
     * Checks if the [tileEntity] can interact with a block at that [pos] using that [item].
     */
    fun canUseBlock(tileEntity: TileEntity, item: ItemStack?, pos: BlockPos): Boolean {
        if (tileEntity.owner == null) {
            return true
        }
        return check0(CanUseBlockTileArgs(tileEntity, item?.clone(), pos.location)) { canUseBlock(tileEntity, item, pos.location) }
    }

    /**
     * Checks if the [player] can interact with a block at that [pos] using that [item].
     */
    fun canUseBlock(player: OfflinePlayer, item: ItemStack?, pos: BlockPos): Boolean {
        return check0(CanUseBlockUserArgs(player, item?.clone(), pos.location)) { canUseBlock(player, item, pos.location) }
    }

    /**
     * Checks if the [player] can interact with a block at that [pos] using that [item].
     */
    fun canUseBlock(player: Player, item: ItemStack?, pos: BlockPos): Boolean {
        return check0(CanUseBlockUserArgs(player, item?.clone(), pos.location)) { canUseBlock(player, item, pos.location) }
    }

    /**
     * Checks if the [tileEntity] can use that [item] at that [location].
     */
    fun canUseItem(tileEntity: TileEntity, item: ItemStack, location: Location): Boolean {
        if (tileEntity.owner == null) {
            return true
        }
        return check0(CanUseItemTileArgs(tileEntity, item.clone(), location)) { canUseItem(tileEntity, item, location) }
    }

    /**
     * Checks if the [player] can use that [item] at that [location].
     */
    fun canUseItem(player: OfflinePlayer, item: ItemStack, location: Location): Boolean {
        return check0(CanUseItemUserArgs(player, item.clone(), location)) { canUseItem(player, item, location) }
    }

    /**
     * Checks if the [player] can use that [item] at that [location].
     */
    fun canUseItem(player: Player, item: ItemStack, location: Location): Boolean {
        return check0(CanUseItemUserArgs(player, item.clone(), location)) { canUseItem(player, item, location) }
    }

    /**
     * Checks if the [tileEntity] can interact with the [entity] wile holding that [item].
     */
    fun canInteractWithEntity(tileEntity: TileEntity, entity: Entity, item: ItemStack?): Boolean {
        if (tileEntity.owner == null) {
            return true
        }
        return check0(CanInteractWithEntityTileArgs(tileEntity, entity, item?.clone())) { canInteractWithEntity(tileEntity, entity, item) }
    }

    /**
     * Checks if the [player] can interact with the [entity] while holding that [item].
     */
    fun canInteractWithEntity(player: OfflinePlayer, entity: Entity, item: ItemStack?): Boolean {
        return check0(CanInteractWithEntityUserArgs(player, entity, item?.clone())) { canInteractWithEntity(player, entity, item) }
    }

    /**
     * Checks if the [player] can interact with the [entity] while holding that [item].
     */
    fun canInteractWithEntity(player: Player, entity: Entity, item: ItemStack?): Boolean {
        return check0(CanInteractWithEntityUserArgs(player, entity, item?.clone())) { canInteractWithEntity(player, entity, item) }
    }

    /**
     * Checks if the [tileEntity] can hurt the [entity] with this [item].
     */
    fun canHurtEntity(tileEntity: TileEntity, entity: Entity, item: ItemStack?): Boolean {
        if (tileEntity.owner == null) {
            return true
        }
        return check0(CanHurtEntityTileArgs(tileEntity, entity, item?.clone())) { canHurtEntity(tileEntity, entity, item) }
    }

    /**
     * Checks if the [player] can hurt the [entity] with this [item].
     */
    fun canHurtEntity(player: OfflinePlayer, entity: Entity, item: ItemStack?): Boolean {
        return check0(CanHurtEntityUserArgs(player, entity, item?.clone())) { canHurtEntity(player, entity, item) }
    }

    /**
     * Checks if the [player] can hurt the [entity] with this [item].
     */
    fun canHurtEntity(player: Player, entity: Entity, item: ItemStack?): Boolean {
        return check0(CanHurtEntityUserArgs(player, entity, item?.clone())) { canHurtEntity(player, entity, item) }
    }

    private fun check0(
        args: ProtectionArgs,
        check: ProtectionIntegration.() -> Boolean,
    ): Boolean {
        if (!NEKO_PLUGIN.isEnabled) {
            return false
        }

        var allowed = true
        for (integration in integrations) {
            allowed = allowed && integration.check()
        }

        return allowed
    }
}
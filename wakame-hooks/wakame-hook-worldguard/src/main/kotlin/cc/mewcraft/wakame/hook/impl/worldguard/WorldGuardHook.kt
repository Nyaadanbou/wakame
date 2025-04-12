package cc.mewcraft.wakame.hook.impl.worldguard

import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.protection.ProtectionIntegration.ExecutionMode
import cc.mewcraft.wakame.integration.Hook
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.world.World
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Hook(plugins = ["WorldGuard"])
object WorldGuardHook : ProtectionIntegration {

    private val PLUGIN get() = WorldGuardPlugin.inst()

    // 在 JavaPlugin#onEnable 阶段, 即使 Koish 是在 WorldGuard 之后, WorldGuard.getInstance().platform 依然可能会返回 null
    // 所以将这个 property 改为 getter 而非 initializer 以解决这个问题
    private val PLATFORM get() = WorldGuard.getInstance().platform

    override fun getExecutionMode(): ExecutionMode {
        return ExecutionMode.NONE
    }

    override fun canBreak(player: OfflinePlayer, item: ItemStack?, location: Location): Boolean {
        return runQuery(player, location, Flags.BLOCK_BREAK)
    }

    override fun canPlace(player: OfflinePlayer, item: ItemStack, location: Location): Boolean {
        return runQuery(player, location, Flags.BLOCK_PLACE)
    }

    override fun canUseBlock(player: OfflinePlayer, item: ItemStack?, location: Location): Boolean {
        return runQuery(player, location, Flags.USE)
    }

    override fun canUseItem(player: OfflinePlayer, item: ItemStack, location: Location): Boolean {
        return runQuery(player, location, Flags.USE)
    }

    override fun canInteractWithEntity(player: OfflinePlayer, entity: Entity, item: ItemStack?): Boolean {
        return runQuery(player, entity.location, Flags.INTERACT)
    }

    override fun canHurtEntity(player: OfflinePlayer, entity: Entity, item: ItemStack?): Boolean {
        if (player is Player && entity is Player) {
            return runQuery(player, entity.location, Flags.PVP)
        }
        return runQuery(player, entity.location, Flags.DAMAGE_ANIMALS)
    }

    // 开发日记 2024/11/24 小米
    // 依然不明白为什么 Nova 会存在离线玩家的权限检查,
    // 难不成一些持续运行的“机器方块”需要检查权限?
    // 这些方块当玩家离线后也会持续运行.
    private fun runQuery(offlinePlayer: OfflinePlayer, location: Location, vararg flags: StateFlag): Boolean {
        val world = BukkitAdapter.adapt(location.world)

        // 离线玩家没有任何权限. 如果玩家离线, 则返回 false
        val onlinePlayer = offlinePlayer.player ?: return false
        val localPlayer = PLUGIN.wrapPlayer(onlinePlayer)

        if (hasBypass(world, localPlayer)) {
            return true
        }

        val query = PLATFORM.regionContainer.createQuery() //
        val queryState = query.queryState(BukkitAdapter.adapt(location), localPlayer, *flags)
        return queryState == null || queryState == StateFlag.State.ALLOW
    }

    private fun hasBypass(world: World, player: LocalPlayer): Boolean {
        return PLATFORM.sessionManager.hasBypass(player, world)
    }

}
package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.protection.ProtectionIntegration.ExecutionMode
import cc.mewcraft.wakame.ecs.FleksPatcher
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.hook.impl.towny.bridge.koishify
import cc.mewcraft.wakame.hook.impl.towny.gui.enhancement.TownEnhancementsMenu
import cc.mewcraft.wakame.hook.impl.towny.system.BuffFurnace
import cc.mewcraft.wakame.hook.impl.towny.system.RemoveTownHall
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.registerEvents
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownyPermission
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

@Hook(plugins = ["Towny"])
object TownyHook : ProtectionIntegration, FleksPatcher, Listener {

    init {
        addToRegistryFamily("towny_families") { TownyFamilies }

        addToRegistrySystem("buff_furnace") { BuffFurnace }
        addToRegistrySystem("remove_town_hall") { RemoveTownHall }
        // MongoTownDataStorage.init()

        registerEvents()
    }

    @EventHandler
    fun on(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (!player.isSneaking) {
            return
        }
        event.isCancelled = true
        TownEnhancementsMenu(
            town = TOWNY.getTown(event.player) ?: return,
            viewer = event.player
        ).open()
    }

    internal val TOWNY = TownyAPI.getInstance()

    internal fun getTownHallEntity(furnace: Block): KoishEntity? {
        val town = TOWNY.getTownBlock(furnace.location)?.townOrNull ?: return null
        return town.koishify()
    }

    override fun getExecutionMode(): ExecutionMode {
        return ExecutionMode.SERVER
    }

    override fun canBreak(player: OfflinePlayer, item: ItemStack?, location: Location): Boolean {
        return hasPermission(player, location, TownyPermission.ActionType.DESTROY)
    }

    override fun canPlace(player: OfflinePlayer, item: ItemStack, location: Location): Boolean {
        return hasPermission(player, location, TownyPermission.ActionType.BUILD)
    }

    override fun canUseBlock(player: OfflinePlayer, item: ItemStack?, location: Location): Boolean {
        return hasPermission(player, location, TownyPermission.ActionType.SWITCH)
    }

    override fun canUseItem(player: OfflinePlayer, item: ItemStack, location: Location): Boolean {
        return hasPermission(player, location, TownyPermission.ActionType.ITEM_USE)
    }

    override fun canInteractWithEntity(player: OfflinePlayer, entity: Entity, item: ItemStack?): Boolean {
        return hasPermission(player, entity.location, TownyPermission.ActionType.ITEM_USE)
    }

    override fun canHurtEntity(player: OfflinePlayer, entity: Entity, item: ItemStack?): Boolean {
        if (player is Player && entity is Player) {
            return TOWNY.isPVP(entity.location)
        }
        return hasPermission(player, entity.location, TownyPermission.ActionType.DESTROY)
    }

    private fun hasPermission(player: OfflinePlayer, location: Location, actionType: TownyPermission.ActionType): Boolean {
        // 开发日记 2024/11/24 小米
        // 为什么 Nova 要在这里写个 FakeOnlinePlayer.create(player, location) ?
        // 以及为什么 ProtectionIntegration 的接口全都是 OfflinePlayer?
        // 难道存在需要检查离线玩家是否拥有保护区的权限?
        // 真的存在 [离线玩家触发权限检查] 这种情况吗?

        val onlinePlayer = player.player
        if (onlinePlayer != null) {
            return PlayerCacheUtil.getCachePermission(onlinePlayer, location, location.block.type, actionType)
        }

        // 离线玩家没有任何权限. 如果玩家离线, 则返回 false
        return false
    }

}
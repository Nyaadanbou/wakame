package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import com.destroystokyo.paper.loottable.LootableInventory
import net.kyori.adventure.key.Key
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

class StopBreakingLootChests : Listener {

    private val config by FEATURE_CONFIG.entryOrElse(emptyMap<Key, StopBreakingLootChestsConfig.Entry>(), "stop_breaking_loot_chests")

    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        val block = event.getBlock()
        val blockState = block.state
        val world = block.world
        if (blockState is LootableInventory && blockState.hasLootTable() &&
            config.containsKey(world.key) && config.getValue(world.key).blockBreak
        ) {
            val player = event.player
            if (player.gameMode !== GameMode.CREATIVE) {
                event.isCancelled = true
                player.sendMessage(TranslatableMessages.MSG_LOOTCHEST_CANNOT_BE_DESTROYED)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onDeath(event: VehicleDestroyEvent) {
        val attacker = event.attacker ?: return
        val vehicle = event.vehicle
        val world = vehicle.world
        if (vehicle is LootableInventory && vehicle.hasLootTable() &&
            config.containsKey(world.key) && config.getValue(world.key).vehicleDestroy
        ) {
            if (attacker is Player && attacker.gameMode !== GameMode.CREATIVE) {
                event.isCancelled = true
                attacker.sendMessage(TranslatableMessages.MSG_LOOTCHEST_CANNOT_BE_DESTROYED)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onExplode(event: BlockExplodeEvent) {
        val world = event.explodedBlockState.world
        handleExplode(ExplodeType.BLOCK, world, event.blockList())
    }

    @EventHandler(ignoreCancelled = true)
    fun onExplode(event: EntityExplodeEvent) {
        val world = event.entity.world
        handleExplode(ExplodeType.ENTITY, world, event.blockList())
    }

    private fun handleExplode(type: ExplodeType, world: World, blockList: MutableList<Block>) {
        val enabled = when (type) {
            ExplodeType.BLOCK -> config.containsKey(world.key) && config.getValue(world.key).blockExplode
            ExplodeType.ENTITY -> config.containsKey(world.key) && config.getValue(world.key).entityExplode
        }
        if (enabled) {
            if (blockList.isEmpty()) return
            for (block in blockList) {
                val blockState = block.state
                if (blockState is LootableInventory && blockState.hasLootTable()) {
                    blockList.remove(block)
                }
            }
        }
    }

    enum class ExplodeType {
        /**
         * @see BlockExplodeEvent
         */
        BLOCK,
        /**
         * @see EntityExplodeEvent
         */
        ENTITY
    }
}

interface StopBreakingLootChestsConfig {

    @ConfigSerializable
    data class Entry(
        val blockBreak: Boolean = false,
        val blockExplode: Boolean = false,
        val entityExplode: Boolean = false,
        val vehicleDestroy: Boolean = false,
    )
}
package cc.mewcraft.wakame.entity.villager

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import net.kyori.adventure.key.Key
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import xyz.xenondevs.commons.provider.orElse

@Init(
    stage = InitStage.POST_WORLD,
)
object VillagerTradeDisabler : Listener {

    init {
        registerEvents()
    }

    private val DISABLED_WORLDS: Set<Key> by MAIN_CONFIG.optionalEntry<Set<Key>>("disable_villager_trade_in_worlds").orElse(setOf())

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun on(event: PlayerInteractEntityEvent) {
        val rightClicked = event.rightClicked
        val rightClickedLocation = rightClicked.location
        val worldKey = rightClickedLocation.world.key
        if (worldKey in DISABLED_WORLDS) {
            event.isCancelled = true
            event.player.playSound(rightClicked, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            event.player.sendMessage(TranslatableMessages.MSG_VILLAGER_TRADE_DISABLED_IN_THIS_WORLD)
        }
    }
}
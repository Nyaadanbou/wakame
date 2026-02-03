package cc.mewcraft.wakame.entity.villager

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.feature.FEATURE_CONFIG
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.adventure.BukkitSound
import cc.mewcraft.wakame.util.registerEvents
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.MerchantInventory

@Init(InitStage.POST_WORLD)
object VillagerTradeDisabler : Listener {

    init {
        registerEvents()
    }

    private val disabledWorlds: Set<Key> by FEATURE_CONFIG.entryOrElse<Set<Key>>(setOf(), "disable_villager_trade_in_worlds")

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun on(event: InventoryOpenEvent) {
        val inventory = event.inventory as? MerchantInventory ?: return
        val villager = inventory.holder as? Villager ?: return
        val location = inventory.location
        val worldKey = location?.world?.key
        if (worldKey != null && worldKey in disabledWorlds) {
            event.isCancelled = true
            event.player.playSound(Sound.sound().type(BukkitSound.ENTITY_VILLAGER_NO).source(Sound.Source.NEUTRAL).build(), villager)
            event.player.sendMessage(TranslatableMessages.MSG_VILLAGER_TRADE_DISABLED_IN_THIS_WORLD)
        }
    }
}
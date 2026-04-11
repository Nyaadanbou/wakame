package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.enchantment.system.EnchantmentAttributeSystem
import cc.mewcraft.wakame.enchantment.system.EnchantmentEffectSystem
import cc.mewcraft.wakame.entity.attribute.system.ItemAttributeSystem
import cc.mewcraft.wakame.integration.auraskills.ManaTraitBridge
import cc.mewcraft.wakame.item.ItemBehaviorListener
import cc.mewcraft.wakame.item.ScanItemSlotChanges
import cc.mewcraft.wakame.item.behavior.impl.weapon.KatanaSwitchSystem
import cc.mewcraft.wakame.item.behavior.impl.weapon.KatanaTickSystem
import cc.mewcraft.wakame.item.feature.SequenceComboFeature
import cc.mewcraft.wakame.kizami.ItemInscriptionSystem
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ServerOnlineUserTicker : Listener {
    @EventHandler
    fun on(event: ServerTickStartEvent) {
        val players = Bukkit.getOnlinePlayers().toList()
        for (player in players) {
            val user = KoishUserManager.get(player)
            if (user.isEmpty) continue
            ScanItemSlotChanges.onTickUser(user, player)
            ItemBehaviorListener.onTickUser(user, player)
            ItemAttributeSystem.onTickUser(user, player)
            ItemInscriptionSystem.onTickUser(user, player)
            EnchantmentEffectSystem.onTickUser(user, player)
            EnchantmentAttributeSystem.onTickUser(user, player)
            KatanaTickSystem.onTickUser(user, player)
            KatanaSwitchSystem.onTickUser(user, player)
            SequenceComboFeature.onTickUser(user, player)
            ManaTraitBridge.onTickUser(user, player)
        }
    }
}
package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.item2.NekoStack
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.component.ItemComponentTypes
import cc.mewcraft.wakame.item2.components.tracks.TrackTypes
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object Trackable : ItemBehavior {

    private val entityKeyLookup: EntityKeyLookup by Injector.inject<EntityKeyLookup>()

    override fun handleAttackEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, damagee: Entity, event: NekoEntityDamageEvent) {
        if (damagee !is LivingEntity) {
            return
        }

        // 以下代码只是个 POC, 用作在游戏内测试 ItemTracks 有没有正常工作

        val entityKey = entityKeyLookup.get(damagee)
        val components = koishStack.components
        val trackMap = components.get(ItemComponentTypes.TRACKS) ?: return

        if (damagee.isDead) {
            val newTracks = trackMap.modify(TrackTypes.ENTITY_KILLS) { it.grow(entityKey, 1) }
            components.set(ItemComponentTypes.TRACKS, newTracks)
        }
    }

}
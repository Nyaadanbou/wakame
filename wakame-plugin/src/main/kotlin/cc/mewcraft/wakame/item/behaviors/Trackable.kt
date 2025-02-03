package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemTracks
import cc.mewcraft.wakame.item.components.tracks.TrackTypes
import cc.mewcraft.wakame.item.projectNeko
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface Trackable : ItemBehavior {
    private object Default : Trackable {
        private val entityKeyLookup: EntityKeyLookup by Injector.inject<EntityKeyLookup>()

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
            if (damagee !is LivingEntity) {
                return
            }

            // 以下代码只是个 POC, 用作在游戏内测试 ItemTracks 有没有正常工作

            val entityKey: Key = entityKeyLookup.get(damagee)
            val stack: NekoStack = itemStack.projectNeko(false)
            val components: ItemComponentMap = stack.components
            val tracks: ItemTracks = components.get(ItemComponentTypes.TRACKS) ?: return

            if (damagee.isDead) {
                val newTracks: ItemTracks = tracks.modify(TrackTypes.ENTITY_KILLS) { track ->
                    track.grow(entityKey)
                }
                components.set(ItemComponentTypes.TRACKS, newTracks)
            }
        }
    }

    companion object Type : ItemBehaviorType<Trackable> {
        override fun create(): Trackable {
            return Default
        }
    }
}
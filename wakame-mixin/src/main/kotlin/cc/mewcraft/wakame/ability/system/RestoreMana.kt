package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object RestoreMana : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, InventoryListenable) }
) {
    override fun onTickEntity(entity: Entity) {
        entity[Mana] += 1
    }
}
package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.combo.PlayerCombo
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

object InitPlayerCombo : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer) }
), FamilyOnAdd {

    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()

        entity.configure {
            it += PlayerCombo(player)
        }
    }

}
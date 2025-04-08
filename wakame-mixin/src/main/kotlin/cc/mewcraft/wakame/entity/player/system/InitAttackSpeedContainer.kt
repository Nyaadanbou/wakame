package cc.mewcraft.wakame.entity.player.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.player.AttackCooldownContainer
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

/**
 * 负责给玩家初始化 [AttackCooldownContainer].
 */
object InitAttackSpeedContainer : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer) }
), FamilyOnAdd {

    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        entity.configure {
            it += AttackCooldownContainer.minecraft(entity[BukkitPlayer].unwrap())
        }
    }

}
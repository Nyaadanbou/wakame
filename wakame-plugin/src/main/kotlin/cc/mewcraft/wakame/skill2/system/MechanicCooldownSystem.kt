package cc.mewcraft.wakame.skill2.system

import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.CooldownComponent
import cc.mewcraft.wakame.ecs.component.Tags
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import net.kyori.adventure.text.Component

class MechanicCooldownSystem : IteratingSystem(
    family = family { all(CooldownComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val cooldown = entity[CooldownComponent].cooldown
        if (cooldown.testCooldown()) {
            // 不在冷却了, 将技能设置成可触发.
            cooldown.elapsed = 0f
            entity.configure { entity -> entity += Tags.CAN_TICK }
        } else {
            // 正在冷却, 将等待时间添加.
            cooldown.elapsed += deltaTime
            sendCooldownTo(entity)
        }
    }

    private fun sendCooldownTo(entity: Entity) {
        val bukkitEntity = entity.getOrNull(BukkitEntityComponent)?.entity ?: return
        val data = entity[CooldownComponent].cooldown

        bukkitEntity.sendMessage(Component.text("剩余时间 tick: ${data.cooldownTime}"))
    }
}
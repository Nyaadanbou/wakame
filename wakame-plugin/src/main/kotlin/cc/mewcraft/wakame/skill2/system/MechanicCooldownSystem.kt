package cc.mewcraft.wakame.skill2.system

import cc.mewcraft.wakame.ecs.component.CasterComponent
import cc.mewcraft.wakame.ecs.component.CooldownComponent
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.Tags
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import net.kyori.adventure.text.Component

class MechanicCooldownSystem : IteratingSystem(
    family = family { all(CooldownComponent, EntityType.SKILL) }
) {
    override fun onTickEntity(entity: Entity) {
        val cooldown = entity[CooldownComponent].cooldown
        if (cooldown.testCooldown()) {
            // 不在冷却了, 将技能设置成可触发.
            cooldown.elapsed = 0f
        } else {
            // 正在冷却, 将等待时间添加.
            entity.configure { entity -> entity -= Tags.CAN_TICK }
            cooldown.elapsed += deltaTime
            sendCooldownTo(entity)
        }
    }

    private fun sendCooldownTo(entity: Entity) {
        val bukkitEntity = entity.getOrNull(CasterComponent)?.entity ?: return
        val data = entity[CooldownComponent].cooldown

        bukkitEntity.sendMessage(Component.text("剩余时间 tick: ${data.cooldownTime}"))
    }
}
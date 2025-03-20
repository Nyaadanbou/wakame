package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.BukkitComponent
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BossBarVisible
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.EntityInfoBossBarComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.util.toStableFloat
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.FamilyOnRemove
import com.github.quillraven.fleks.IteratingSystem
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.Style
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class EntityInfoBossBar : IteratingSystem(
    family = Families.BUKKIT_ENTITY
), FamilyOnAdd, FamilyOnRemove {

    override fun onTickEntity(entity: Entity) {
        val bossBar = entity[EntityInfoBossBarComponent].bossBar

        tickEntityHealth(entity, bossBar)
        tickElementStacks(entity, bossBar)
    }

    private fun tickEntityHealth(entity: Entity, bossBar: BossBar) {
        val bukkitEntity = entity[BukkitEntityComponent].bukkitEntity as? LivingEntity ?: return
        val entityMaxHealth = AttributeMapAccess.instance().get(bukkitEntity).getOrNull()?.getInstance(Attributes.MAX_HEALTH)?.getValue() ?: return
        val progress = bukkitEntity.health / entityMaxHealth
        bossBar.progress(progress.toStableFloat())
    }

    private fun tickElementStacks(entity: Entity, bossBar: BossBar) {
        val bukkitEntity = entity[BukkitEntityComponent].bukkitEntity as? LivingEntity ?: return
        val container = entity.getOrNull(ElementStackContainer)?.elementStacks() ?: return
        val elementStackMessage = text {
            for ((elementEntry, entity) in container) {
                val element = elementEntry.value
                val stack = entity[ElementStackComponent]

                append(
                    BukkitComponent.text()
                        .content("${stack.amount} ")
                        .append(element.displayName)
                        .style(Style.style(*element.displayStyles))
                )
            }
        }

        bukkitEntity.name()
            .appendSpace()
            .append(elementStackMessage)
            .let { bossBar.name(it) }
    }

    override fun onAddEntity(entity: Entity) {
        entity[BukkitEntityComponent].bukkitEntity as? LivingEntity ?: return
        entity.configure {
            it += EntityInfoBossBarComponent()
        }
    }

    override fun onRemoveEntity(entity: Entity) {
        val bossBar = entity[EntityInfoBossBarComponent].bossBar
        for (viewer in bossBar.viewers()) {
            (viewer as Player).koishify()[BossBarVisible].bossBar2DurationTick[bossBar] = 0
        }
    }
}

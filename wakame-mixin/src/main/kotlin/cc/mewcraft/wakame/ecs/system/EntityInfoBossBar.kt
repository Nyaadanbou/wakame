package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.attribute.AttributeProvider
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.BukkitComponent
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.AttributeMapComponent
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
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class EntityInfoBossBar : IteratingSystem(
    family = Families.BUKKIT_ENTITY
), FamilyOnAdd, FamilyOnRemove {

    override fun onTickEntity(entity: Entity) {
        val bukkitEntity = entity[BukkitEntityComponent].bukkitEntity as? LivingEntity ?: return
        val bossBar = entity[EntityInfoBossBarComponent].bossBar

        val progress = getEntityHealthProgress(entity)
        val bossBarName = bukkitEntity.name()
            .appendSpace()
            .append(getEntityHealthCompoment(entity))
            .appendSpace()
            .append(getElementStackComponent(entity))

        when {
            progress < 0.25f -> {
                bossBar.color(BossBar.Color.RED)
                bossBar.overlay(BossBar.Overlay.PROGRESS)
            }

            progress < 0.5f -> {
                bossBar.color(BossBar.Color.YELLOW)
                bossBar.overlay(BossBar.Overlay.PROGRESS)
            }

            else -> {
                bossBar.color(BossBar.Color.GREEN)
                bossBar.overlay(BossBar.Overlay.PROGRESS)
            }
        }

        bossBar.progress(progress)
        bossBar.name(bossBarName)
    }

    private fun getEntityHealthProgress(entity: Entity): Float {
        val bukkitEntity = entity[BukkitEntityComponent].bukkitEntity as? LivingEntity ?: return 0f
        val entityMaxHealth = getMaxHealth(entity) ?: return 0f
        val progress = bukkitEntity.health / entityMaxHealth
        return progress.toStableFloat()
    }

    private fun getEntityHealthCompoment(entity: Entity): BukkitComponent {
        val bukkitEntity = entity[BukkitEntityComponent].bukkitEntity as? LivingEntity ?: return BukkitComponent.empty()
        val entityMaxHealth = getMaxHealth(entity) ?: return BukkitComponent.empty()
        return BukkitComponent.text()
            .append(
                BukkitComponent.text()
                    .content("${bukkitEntity.health.toInt()}")
                    .color(TextColor.color(0xFFC0CB))
            )
            .append(
                BukkitComponent.text()
                    .content("/")
                    .color(NamedTextColor.GRAY)
            )
            .append(
                BukkitComponent.text()
                    .content("${entityMaxHealth.toInt()}")
                    .color(TextColor.color(0xFFF1CB))
            )
            .build()
    }

    private fun getElementStackComponent(entity: Entity): BukkitComponent {
        val container = entity.getOrNull(ElementStackContainer)?.elementStacks() ?: return BukkitComponent.empty()
        val elementStackMessage = BukkitComponent.text()

        for ((elementEntry, entity) in container) {
            val element = elementEntry.value
            val stack = entity[ElementStackComponent]

            elementStackMessage.append(
                BukkitComponent.text()
                    .content("${stack.amount} ")
                    .append(element.displayName)
                    .style(Style.style(*element.displayStyles))
            )
        }

        return elementStackMessage.build()
    }

    private fun getMaxHealth(entity: Entity): Double? {
        val attributeMap = entity[AttributeMapComponent].invoke()
        val maxHealth = AttributeProvider.INSTANCE.get("max_health") ?: return null
        return attributeMap.getInstance(maxHealth)?.getValue()
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

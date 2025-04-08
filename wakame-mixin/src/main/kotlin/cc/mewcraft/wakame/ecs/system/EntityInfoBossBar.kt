package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BossBarVisible
import cc.mewcraft.wakame.ecs.component.BukkitEntity
import cc.mewcraft.wakame.ecs.component.EntityInfoBossBarComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.util.toStableFloat
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.FamilyOnRemove
import com.github.quillraven.fleks.IteratingSystem
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class EntityInfoBossBar : IteratingSystem(
    family = Families.BUKKIT_ENTITY
), FamilyOnAdd, FamilyOnRemove {

    override fun onTickEntity(eentity: EEntity) {
        val entity = eentity[BukkitEntity].unwrap() as? LivingEntity ?: return
        val bossBar = eentity[EntityInfoBossBarComponent].bossBar

        val progress = getEntityHealthProgress(eentity)
        val bossBarName = entity.name()
            .appendSpace()
            .append(getEntityHealthComponent(eentity))
            .appendSpace()
            .append(getElementStackComponent(eentity))

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

    private fun getEntityHealthProgress(eentity: EEntity): Float {
        val entity = eentity[BukkitEntity].unwrap() as? LivingEntity ?: return 0f
        val entityMaxHealth = getMaxHealth(eentity) ?: return 0f
        val progress = entity.health / entityMaxHealth
        return progress.toStableFloat()
    }

    private fun getEntityHealthComponent(eentity: EEntity): Component {
        val entity = eentity[BukkitEntity].unwrap() as? LivingEntity ?: return Component.empty()
        val entityMaxHealth = getMaxHealth(eentity) ?: return Component.empty()
        return Component.text()
            .append(
                Component.text()
                    .content("${entity.health.toInt()}")
                    .color(TextColor.color(0xFFC0CB))
            )
            .append(
                Component.text()
                    .content("/")
                    .color(NamedTextColor.GRAY)
            )
            .append(
                Component.text()
                    .content("${entityMaxHealth.toInt()}")
                    .color(TextColor.color(0xFFF1CB))
            )
            .build()
    }

    private fun getElementStackComponent(eentity: EEntity): Component {
        val container = eentity.getOrNull(ElementStackContainer)?.elementStacks() ?: return Component.empty()
        val elementStackMessage = Component.text()

        for ((elementEntry, entity) in container) {
            val element = elementEntry.unwrap()
            val stack = entity[ElementStackComponent]

            elementStackMessage.append(
                Component.text()
                    .content("${stack.amount} ")
                    .append(element.displayName)
                    .style(Style.style(*element.displayStyles))
            )
        }

        return elementStackMessage.build()
    }

    private fun getMaxHealth(eentity: EEntity): Double? {
        val attributeContainer = eentity[AttributeMap]
        return attributeContainer.getInstance(Attributes.MAX_HEALTH)?.getValue()
    }

    override fun onAddEntity(eentity: Entity) {
        eentity[BukkitEntity].unwrap() as? LivingEntity ?: return
        eentity.configure {
            it += EntityInfoBossBarComponent()
        }
    }

    override fun onRemoveEntity(eentity: EEntity) {
        // FIXME #375: Caused by: cc.mewcraft.wakame.shaded.fleks.FleksNoSuchEntityComponentException: Entity 'Entity(id=0, version=0)' has no component of type 'BossBarVisible'.
        val bossBar = eentity[EntityInfoBossBarComponent].bossBar
        for (viewer in bossBar.viewers()) {
            (viewer as Player).koishify()[BossBarVisible].bossBar2DurationTick[bossBar] = 0
        }
    }
}

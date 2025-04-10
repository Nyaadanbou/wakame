package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.bridge.isKoishfiable
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BossBarVisible
import cc.mewcraft.wakame.ecs.component.BukkitEntity
import cc.mewcraft.wakame.ecs.component.EntityInfoBossBar
import cc.mewcraft.wakame.element.component.ElementStack
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.Attributes
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

object EntityInfoBossBarManager : IteratingSystem(
    family = Families.BUKKIT_ENTITY
), FamilyOnAdd, FamilyOnRemove {

    override fun onTickEntity(eentity: EEntity) {
        val entity = eentity[BukkitEntity].unwrap()
        val bossBar = eentity[EntityInfoBossBar].bossBar

        val progress = getEntityHealthProgress(eentity)
        val bossBarName = entity.name()
            .append(getEntityHealthComponent(eentity))
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
        val entityMaxHealth = getMaxHealth(eentity).takeUnless(Float::isNaN) ?: return 0f
        val progress = entity.health.toFloat() / entityMaxHealth
        return progress.coerceIn(0f, 1f)
    }

    private fun getEntityHealthComponent(eentity: EEntity): Component {
        val entity = eentity[BukkitEntity].unwrap() as? LivingEntity ?: return Component.empty()
        val entityMaxHealth = getMaxHealth(eentity).takeUnless(Float::isNaN) ?: return Component.empty()
        return Component.text()
            .appendSpace()
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
        val iterator = container.iterator()
        val elementStackMessage = Component.text()

        while (iterator.hasNext()) {
            val (elementEntry, elementEntity) = iterator.next()
            val element = elementEntry.unwrap()
            val stack = elementEntity[ElementStack]

            elementStackMessage.appendSpace()
            val elementName = element.displayName
                .appendSpace()
                .append(
                    Component.text()
                        .content("${stack.amount} ")
                        .style(Style.style(*element.displayStyles))
                )
            elementStackMessage.append(elementName)
        }

        return elementStackMessage.build()
    }

    private fun getMaxHealth(eentity: EEntity): Float {
        val attributeContainer = eentity[AttributeMap]
        return attributeContainer.getInstance(Attributes.MAX_HEALTH)?.getValue()?.toFloat() ?: Float.NaN
    }

    override fun onAddEntity(eentity: Entity) {
        eentity.configure { it += EntityInfoBossBar() }
    }

    override fun onRemoveEntity(eentity: EEntity) {
        val bossBar = eentity[EntityInfoBossBar].bossBar
        for (viewer in bossBar.viewers()) {
            val player = viewer as Player
            if (!player.isKoishfiable()) {
                continue
            }
            player.koishify().unwrap()[BossBarVisible].bossBar2DurationTick[bossBar] = 0
        }
    }
}

package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.isNeko
import cc.mewcraft.wakame.item.rarity
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.rarity.GlowColor
import cc.mewcraft.wakame.util.itemName
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.text.Component
import org.bukkit.entity.Item
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 修改 [Item].
 */
internal class ItemEntityRenderer : PacketListenerAbstract() {

    // 目前用于彩色物品发光的实现
    private val entityId2EntityUniqueId = ConcurrentHashMap<Int, UUID>()

    override fun onPacketSend(event: PacketSendEvent) {
        when (event.packetType) {
            PacketType.Play.Server.ENTITY_METADATA -> {
                val wrapper = WrapperPlayServerEntityMetadata(event)

                val item = SpigotConversionUtil.getEntityById(null, wrapper.entityId) as? Item ?: return

                var changed = false

                changed = handleCustomName(event, wrapper, item) || changed
                changed = handleGlowEffect(event, wrapper, item) || changed

                if (changed) {
                    event.markForReEncode(true)
                }
            }

            PacketType.Play.Server.DESTROY_ENTITIES -> {
                val wrapper = WrapperPlayServerDestroyEntities(event)
                handleGlowEffect(event, wrapper)
            }
        }
    }

    private fun handleCustomName(event: PacketSendEvent, wrapper: WrapperPlayServerEntityMetadata, item: Item): Boolean {
        val changed = tryAddCustomNameEntityData(item, wrapper.entityMetadata)
        return changed
    }

    private fun handleGlowEffect(event: PacketSendEvent, wrapper: WrapperPlayServerEntityMetadata, item: Item): Boolean {
        val changed = tryAddGlowEffectEntityData(item, wrapper.entityMetadata)
        sendGlowColorPacket(event, item)
        return changed
    }

    private fun handleGlowEffect(event: PacketSendEvent, wrapper: WrapperPlayServerDestroyEntities) {
        for (entityId in wrapper.entityIds) {
            entityId2EntityUniqueId.remove(entityId)
                ?.let { entityUniqueId -> buildRemoveTeamPacket(entityUniqueId) }
                ?.let { serverTeamPacket -> event.user.sendPacketSilently(serverTeamPacket) }
        }
    }

    /**
     * @return 如果成功添加了数据则返回 `true`, 否则返回 `false`
     */
    private fun tryAddGlowEffectEntityData(entity: Item, entityData: MutableList<EntityData>): Boolean {
        val nekoStack = entity.itemStack.shadowNeko() ?: return false
        val templates = nekoStack.templates
        if (!templates.has(ItemTemplateTypes.GLOWABLE))
            return false

        // Glow effect flag (0x40)
        entityData.add(EntityData(0, EntityDataTypes.BYTE, 0x40.toByte()))

        return true
    }

    /**
     * @return 如果成功添加了数据则返回 `true`, 否则返回 `false`
     */
    private fun tryAddCustomNameEntityData(entity: Item, entityData: MutableList<EntityData>): Boolean {
        val itemStack = entity.itemStack
        if (!itemStack.isNeko)
            return false

        // CustomName
        entityData.add(EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.ofNullable(itemStack.itemName)))

        // CustomNameVisible
        entityData.add(EntityData(3, EntityDataTypes.BOOLEAN, true))

        return true
    }

    private fun sendGlowColorPacket(event: PacketSendEvent, entity: Item) {
        val nekoStack = entity.itemStack.shadowNeko() ?: return
        val user = event.user
        val rarityColor = nekoStack.rarity.value.glowColor.takeIf { it != GlowColor.empty() } ?: return
        val teamPacket = buildCreateTeamPacket(entity, rarityColor)
        entityId2EntityUniqueId[entity.entityId] = entity.uniqueId
        user.sendPacketSilently(teamPacket)
    }

    private fun buildCreateTeamPacket(itemEntity: Item, color: GlowColor): WrapperPlayServerTeams {
        val entityUniqueId = itemEntity.uniqueId
        return WrapperPlayServerTeams(
            /* teamName = */ "glow_item_$entityUniqueId",
            /* teamMode = */ WrapperPlayServerTeams.TeamMode.CREATE,
            /* teamInfo = */ WrapperPlayServerTeams.ScoreBoardTeamInfo(
                /* displayName = */ Component.empty(),
                /* prefix = */ null,
                /* suffix = */ null,
                /* tagVisibility = */ WrapperPlayServerTeams.NameTagVisibility.NEVER,
                /* collisionRule = */ WrapperPlayServerTeams.CollisionRule.NEVER,
                /* color = */ color.color,
                /* optionData = */ WrapperPlayServerTeams.OptionData.NONE
            ),
            /* entities = */ listOf(entityUniqueId.toString())
        )
    }

    private fun buildRemoveTeamPacket(entityUniqueId: UUID): WrapperPlayServerTeams {
        return WrapperPlayServerTeams(
            /* teamName = */ "glow_item_$entityUniqueId",
            /* teamMode = */ WrapperPlayServerTeams.TeamMode.REMOVE,
            /* teamInfo = */ null as WrapperPlayServerTeams.ScoreBoardTeamInfo?,
        )
    }
}
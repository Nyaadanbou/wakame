package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.isNeko
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.rarity.GlowColor
import cc.mewcraft.wakame.util.backingItemName
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.kyori.adventure.text.Component
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class ItemEntityDisplayPacketHandler : PacketListenerAbstract() {

    private val teamEntityId2entityUniqueId = Int2ObjectOpenHashMap<UUID>()

    override fun onPacketSend(event: PacketSendEvent) {
        when (event.packetType) {
            PacketType.Play.Server.ENTITY_METADATA -> {
                val origin = WrapperPlayServerEntityMetadata(event)
                val entity = SpigotConversionUtil.getEntityById(null, origin.entityId) as? Item ?: return

                val entityData = ArrayList<EntityData>()
                addItemNameTagEntityData(entity, entityData)
                addGlowItemEntityData(entity, entityData)

                if (entityData.isNotEmpty()) {
                    val metadataPacket = WrapperPlayServerEntityMetadata(origin.entityId, entityData)
                    event.user.sendPacketSilently(metadataPacket)
                    sendGlowColorPacket(event, entity)
                }
            }

            PacketType.Play.Server.DESTROY_ENTITIES -> {
                val origin = WrapperPlayServerDestroyEntities(event)

                for (entityId in origin.entityIds) {
                    teamEntityId2entityUniqueId.remove(entityId)
                        ?.let { buildRemoveTeamPacket(it) }
                        ?.let { event.user.sendPacketSilently(it) }
                }
            }
        }
    }

    private fun addGlowItemEntityData(entity: Item, entityData: ArrayList<EntityData>) {
        val nekoStack = entity.itemStack.tryNekoStack ?: return
        val components = nekoStack.components
        if (!components.has(ItemComponentTypes.GLOWABLE))
            return

        entityData.add(EntityData(0, EntityDataTypes.BYTE, 0x40.toByte())) // Glow effect flag (0x40)
    }

    private fun addItemNameTagEntityData(entity: Item, entityData: ArrayList<EntityData>) {
        val itemStack = entity.itemStack
        if (!itemStack.isNeko) {
            return
        }
        entityData.add(EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.ofNullable(itemStack.backingItemName))) // CustomName
        entityData.add(EntityData(3, EntityDataTypes.BOOLEAN, true)) // CustomNameVisible
    }

    private fun sendGlowColorPacket(event: PacketSendEvent, entity: Item) {
        val nekoStack = entity.itemStack.tryNekoStack ?: return
        val components = nekoStack.components
        val rarityColor = components.get(ItemComponentTypes.RARITY)?.rarity?.glowColor
            ?.takeIf { it != GlowColor.empty() }
            ?: return
        val teamPacket = buildCreateTeamPacket(entity, rarityColor)
        teamEntityId2entityUniqueId[entity.entityId] = entity.uniqueId
        event.user.sendPacketSilently(teamPacket)
    }

    private fun buildCreateTeamPacket(itemEntity: Item, color: GlowColor): WrapperPlayServerTeams {
        val entityUniqueId = itemEntity.uniqueId
        return WrapperPlayServerTeams(
            "glow_item_$entityUniqueId",
            WrapperPlayServerTeams.TeamMode.CREATE,
            WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                null,
                null,
                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                color.color,
                WrapperPlayServerTeams.OptionData.NONE
            ),
            listOf(entityUniqueId.toString())
        )
    }

    private fun buildRemoveTeamPacket(entityUniqueIds: UUID): WrapperPlayServerTeams {
        return WrapperPlayServerTeams(
            "glow_item_$entityUniqueIds",
            WrapperPlayServerTeams.TeamMode.REMOVE,
            null as WrapperPlayServerTeams.ScoreBoardTeamInfo?,
        )
    }
}
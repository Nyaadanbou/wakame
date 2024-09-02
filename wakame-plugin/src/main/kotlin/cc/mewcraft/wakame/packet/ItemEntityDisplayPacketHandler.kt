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

class ItemEntityDisplayPacketHandler : PacketListenerAbstract() {

    private val teamEntityId2entityUniqueId = Int2ObjectOpenHashMap<UUID>()

    override fun onPacketSend(event: PacketSendEvent) {
        if (event.player !is Player) return

        when (event.packetType) {
            PacketType.Play.Server.ENTITY_METADATA -> {
                val origin = WrapperPlayServerEntityMetadata(event)
                val entity = SpigotConversionUtil.getEntityById(null, origin.entityId) as? Item ?: return
                if (sendGlowItemPacket(event, entity)) {
                    sendGlowColorPacket(event, entity)
                }

                sendItemNameTagPacket(event, entity)
            }

            PacketType.Play.Server.DESTROY_ENTITIES -> {
                val origin = WrapperPlayServerDestroyEntities(event)
                origin.entityIds
                    .asSequence()
                    .mapNotNull { teamEntityId2entityUniqueId.remove(it) }
                    .map { removeTeamPacket(it) }
                    .forEach { event.user.sendPacketSilently(it) }
            }
        }
    }

    private fun sendGlowItemPacket(event: PacketSendEvent, entity: Item): Boolean {
        val nekoStack = entity.itemStack.tryNekoStack ?: return false
        val itemComponents = nekoStack.components
        if (!itemComponents.has(ItemComponentTypes.GLOWABLE))
            return false

        val metadataPacket = WrapperPlayServerEntityMetadata(
            entity.entityId,
            listOf(
                EntityData(0, EntityDataTypes.BYTE, 0x40.toByte()),
                EntityData(3, EntityDataTypes.BOOLEAN, true)
            )
        )

        event.user.sendPacketSilently(metadataPacket)
        return true
    }

    private fun sendGlowColorPacket(event: PacketSendEvent, entity: Item) {
        val nekoStack = entity.itemStack.tryNekoStack ?: return
        val itemComponents = nekoStack.components
        val rarityColor = itemComponents.get(ItemComponentTypes.RARITY)?.rarity?.glowColor
            ?.takeIf { it != GlowColor.empty() }
            ?: return
        val teamPacket = createTeamPacket(entity, rarityColor)
        teamEntityId2entityUniqueId[entity.entityId] = entity.uniqueId
        event.user.sendPacketSilently(teamPacket)
    }

    private fun sendItemNameTagPacket(event: PacketSendEvent, entity: Item) {
        val itemStack = entity.itemStack
        if (!itemStack.isNeko) {
            return
        }

        val metadataPacket = WrapperPlayServerEntityMetadata(
            entity.entityId,
            listOf(
                EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.ofNullable(itemStack.backingItemName))
            )
        )

        event.user.sendPacketSilently(metadataPacket)
    }

    private fun createTeamPacket(itemEntity: Item, color: GlowColor): WrapperPlayServerTeams {
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

    private fun removeTeamPacket(entityUniqueIds: UUID): WrapperPlayServerTeams {
        return WrapperPlayServerTeams(
            "glow_item_$entityUniqueIds",
            WrapperPlayServerTeams.TeamMode.REMOVE,
            null as WrapperPlayServerTeams.ScoreBoardTeamInfo?,
        )
    }
}
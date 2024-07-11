package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.rarity.GlowColor
import cc.mewcraft.wakame.util.NmsEntityUtils
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import net.kyori.adventure.text.Component
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class GlowingItemPacketHandler : PacketListenerAbstract() {

    private val entityId2entityUniqueId: MutableMap<Int, UUID> = ConcurrentHashMap()

    override fun onPacketSend(event: PacketSendEvent) {
        if (event.player !is Player) return

        when (event.packetType) {
            PacketType.Play.Server.ENTITY_METADATA -> {
                val origin = WrapperPlayServerEntityMetadata(event)
                val entity = NmsEntityUtils.getEntity(origin.entityId) as? Item ?: return
                val nekoStack = entity.itemStack.tryNekoStack ?: return
                val rarityColor = nekoStack.components.get(ItemComponentTypes.RARITY)?.rarity?.glowColor
                    ?.takeIf { it != GlowColor.empty() }
                    ?: return

                val metadataPacket = WrapperPlayServerEntityMetadata(
                    entity.entityId,
                    listOf(EntityData(0, EntityDataTypes.BYTE, 0x40.toByte()))
                )
                val teamPacket = createTeamPacket(entity, rarityColor)
                entityId2entityUniqueId[entity.entityId] = entity.uniqueId

                with(event.user) {
                    sendPacketSilently(metadataPacket)
                    sendPacketSilently(teamPacket)
                }
            }

            PacketType.Play.Server.DESTROY_ENTITIES -> {
                val origin = WrapperPlayServerDestroyEntities(event)
                origin.entityIds
                    .asSequence()
                    .mapNotNull { entityId2entityUniqueId.remove(it) }
                    .map { removeTeamPacket(it) }
                    .forEach { event.user.sendPacketSilently(it) }
            }
        }
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
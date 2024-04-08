package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
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
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class GlowingItemPacketHandler : PacketListenerAbstract() {

    private val entityId2entityUniqueId: MutableMap<Int, UUID> = ConcurrentHashMap()

    override fun onPacketSend(event: PacketSendEvent) {
        val player = event.player as? Player ?: return
        // if (player.disable) return

        when (event.packetType) {
            PacketType.Play.Server.ENTITY_METADATA -> {
                val origin = WrapperPlayServerEntityMetadata(event)
                val entity = NmsEntityUtils.getEntity(origin.entityId) as? Item ?: return
                val nekoStack = PlayNekoStackFactory.maybe(entity.itemStack) ?: return

                val metadataPacket = WrapperPlayServerEntityMetadata(
                    entity.entityId,
                    listOf(EntityData(0, EntityDataTypes.BYTE, 0x40.toByte()))
                )
                val teamPacket = createTeamPacket(entity, nekoStack)
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

    private fun createTeamPacket(itemEntity: Item, nekoStack: NekoStack): WrapperPlayServerTeams {
        val entityUniqueId = itemEntity.uniqueId
        // TODO: 根据 NekoStack 设置颜色
        val color = NamedTextColor.nearestTo(TextColor.color(Random(nekoStack.seed).nextInt(0xFFFFFF)))

        return WrapperPlayServerTeams(
            "glow_item_$entityUniqueId",
            WrapperPlayServerTeams.TeamMode.CREATE,
            WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                null,
                null,
                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                color,
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
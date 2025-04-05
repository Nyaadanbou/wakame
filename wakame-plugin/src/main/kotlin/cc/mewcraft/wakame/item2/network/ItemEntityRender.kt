package cc.mewcraft.wakame.item2.network

import cc.mewcraft.wakame.item.extension.rarity
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.network.event.PacketHandler
import cc.mewcraft.wakame.network.event.PacketListener
import cc.mewcraft.wakame.network.event.clientbound.ClientboundRemoveEntitiesEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEntityDataPacketEvent
import cc.mewcraft.wakame.network.event.registerPacketListener
import cc.mewcraft.wakame.network.event.unregisterPacketListener
import cc.mewcraft.wakame.shadow.world.entity.ShadowEntity
import cc.mewcraft.wakame.util.NMSUtils
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import cc.mewcraft.wakame.util.connection
import cc.mewcraft.wakame.util.item.itemName
import io.papermc.paper.adventure.PaperAdventure
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.staticShadow
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.ChatFormatting
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 修改 [Item].
 */
@Init(stage = InitStage.POST_WORLD)
internal object ItemEntityRender : PacketListener {

    private val shadowEntity: ShadowEntity = BukkitShadowFactory.global().staticShadow<ShadowEntity>()

    // 目前用于彩色物品发光的实现
    private val entityId2EntityUniqueId = ConcurrentHashMap<Int, UUID>()

    @InitFun
    fun init() {
        registerPacketListener()
    }

    @DisableFun
    fun disable() {
        unregisterPacketListener()
    }

    @PacketHandler
    private fun handleSetEntityData(event: ClientboundSetEntityDataPacketEvent) {
        val oldData = event.packedItems
        val item = NMSUtils.getEntity(event.id) as? Item
          if (item == null || item.itemStack.wrap() == null)
            return
        val newData = oldData.toMutableList()
        tryAddCustomNameEntityData(item, newData)
        tryAddGlowEffectEntityData(item, newData)
        event.packedItems = newData
        sendGlowColorPacket(event.player, item)
    }

    @PacketHandler
    private fun handleEntitiesRemove(event: ClientboundRemoveEntitiesEvent) {
        for (entityId in event.entityIds.intIterator()) {
            entityId2EntityUniqueId.remove(entityId)
                ?.let { entityUniqueId -> buildRemoveTeamPacket(entityUniqueId) }
                ?.let { serverTeamPacket -> event.player.connection.send(serverTeamPacket) }
        }
    }

    private fun tryAddGlowEffectEntityData(item: Item, entityData: MutableList<SynchedEntityData.DataValue<*>>) {
        val koishStack = item.itemStack.wrap()!!
        val templates = koishStack.templates
        if (!templates.has(ItemTemplateTypes.GLOWABLE))
            return

        // Glow effect flag
        entityData.add(SynchedEntityData.DataValue.create(shadowEntity.DATA_SHARED_FLAGS_ID, 0x40))
    }

    private fun tryAddCustomNameEntityData(item: Item, entityData: MutableList<SynchedEntityData.DataValue<*>>) {
        val itemStack = item.itemStack

        // CustomName
        entityData.add(SynchedEntityData.DataValue.create(shadowEntity.DATA_CUSTOM_NAME, Optional.ofNullable(itemStack.itemName?.toNMSComponent())))

        // CustomNameVisible
        entityData.add(SynchedEntityData.DataValue.create(shadowEntity.DATA_CUSTOM_NAME_VISIBLE, true))
    }

    private fun sendGlowColorPacket(player: Player, entity: Item) {
        val koishStack = entity.itemStack.wrap() ?: return
        val rarityColor = koishStack.rarity.unwrap().color ?: return
        val teamPacket = buildCreateTeamPacket(entity, rarityColor)
        entityId2EntityUniqueId[entity.entityId] = entity.uniqueId
        player.connection.send(teamPacket)
    }

    private fun buildCreateTeamPacket(itemEntity: Item, color: NamedTextColor): ClientboundSetPlayerTeamPacket {
        val entityUniqueId = itemEntity.uniqueId
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(
            /* team = */ PlayerTeam(Scoreboard(), "glow_item_$entityUniqueId").apply {
                this.color = PaperAdventure.asVanilla(color) ?: ChatFormatting.RESET
                this.players += entityUniqueId.toString()
            },
            /* updatePlayers = */ true
        )
    }

    private fun buildRemoveTeamPacket(entityUniqueId: UUID): ClientboundSetPlayerTeamPacket {
        return ClientboundSetPlayerTeamPacket.createRemovePacket(
            /* team = */ PlayerTeam(
                Scoreboard(),
                "glow_item_$entityUniqueId"
            ).also {
                it.players += entityUniqueId.toString()
            }
        )
    }
}
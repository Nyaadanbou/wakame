package cc.mewcraft.wakame.item.network

import cc.mewcraft.wakame.item.extension.rarity2
import cc.mewcraft.wakame.item.hasProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
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
import cc.mewcraft.wakame.util.item.itemName
import cc.mewcraft.wakame.util.send
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

    private val SHADOW_ENTITY: ShadowEntity = BukkitShadowFactory.global().staticShadow<ShadowEntity>()

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
        val item = NMSUtils.getEntity(event.id) as? Item ?: return
        val old = event.packedItems
        val new = old.toMutableList().apply {
            tryAddCustomNameEntityData(item, this)
            tryAddGlowEffectEntityData(item, this)
        }
        event.packedItems = new
        sendGlowColorPacket(event.player, item)
    }

    @PacketHandler
    private fun handleEntitiesRemove(event: ClientboundRemoveEntitiesEvent) {
        for (entityId in event.entityIds.intIterator()) {
            entityId2EntityUniqueId.remove(entityId)
                ?.let { entityUniqueId -> buildRemoveTeamPacket(entityUniqueId) }
                ?.also { serverTeamPacket -> event.player.send(serverTeamPacket) }
        }
    }

    private fun tryAddGlowEffectEntityData(item: Item, entityData: MutableList<SynchedEntityData.DataValue<*>>) {
        if (!item.itemStack.hasProp(ItemPropTypes.GLOWABLE)) return
        // Glow effect flag
        entityData.add(SynchedEntityData.DataValue.create(SHADOW_ENTITY.DATA_SHARED_FLAGS_ID, 0x40))
    }

    private fun tryAddCustomNameEntityData(item: Item, entityData: MutableList<SynchedEntityData.DataValue<*>>) {
        // add CustomName
        entityData.add(SynchedEntityData.DataValue.create(SHADOW_ENTITY.DATA_CUSTOM_NAME, Optional.ofNullable(item.itemStack.itemName?.toNMSComponent())))
        // add CustomNameVisible
        entityData.add(SynchedEntityData.DataValue.create(SHADOW_ENTITY.DATA_CUSTOM_NAME_VISIBLE, true))
    }

    private fun sendGlowColorPacket(player: Player, item: Item) {
        val rarityColor = item.itemStack.rarity2?.unwrap()?.color ?: return
        val teamPacket = buildCreateTeamPacket(item, rarityColor)
        entityId2EntityUniqueId[item.entityId] = item.uniqueId
        player.send(teamPacket)
    }

    private fun buildCreateTeamPacket(item: Item, color: NamedTextColor): ClientboundSetPlayerTeamPacket {
        val entityUuid = item.uniqueId
        val playerTeam = PlayerTeam(Scoreboard(), "glow_item_$entityUuid").apply {
            this.color = PaperAdventure.asVanilla(color) ?: ChatFormatting.RESET
            this.players += entityUuid.toString()
        }
        val useAdd = true
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, useAdd)
    }

    private fun buildRemoveTeamPacket(entityUniqueId: UUID): ClientboundSetPlayerTeamPacket {
        val playerTeam = PlayerTeam(
            Scoreboard(),
            "glow_item_$entityUniqueId"
        ).apply {
            this.players += entityUniqueId.toString()
        }
        return ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam)
    }
}
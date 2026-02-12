package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.network.event.PacketHandler
import cc.mewcraft.wakame.network.event.PacketListener
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEntityDataPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundUpdateAttributesPacketEvent
import cc.mewcraft.wakame.network.event.registerPacketListener
import cc.mewcraft.wakame.network.event.unregisterPacketListener
import cc.mewcraft.wakame.shadow.world.entity.ShadowPlayer
import cc.mewcraft.wakame.util.NMSUtils
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.staticShadow
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.Identifier
import org.bukkit.entity.Player

@Init(InitStage.POST_WORLD)
internal object AttributePacketInterceptor : PacketListener {

    private val SHADOW_PLAYER: ShadowPlayer = BukkitShadowFactory.global().staticShadow()

    private val REMOVE_ATTRIBUTES_ON_CLIENTBOUND_UPDATE_ATTRIBUTES_PACKET: Set<Identifier> by MAIN_CONFIG.entryOrElse(setOf(), "remove_attributes_on_clientbound_update_attributes_packet")
    private val REMOVE_ABSORPTION_ON_CLIENTBOUND_SET_ENTITY_DATA_PACKET: Boolean by MAIN_CONFIG.entryOrElse(true, "remove_absorption_on_clientbound_set_entity_data_packet")

    @InitFun
    fun init() {
        registerPacketListener()
    }

    @DisableFun
    fun disable() {
        unregisterPacketListener()
    }

    /**
     * 作用:
     *
     * - 移除 ClientboundUpdateAttributesPacket 中的指定属性
     */
    @PacketHandler
    private fun handleClientboundUpdateAttributes(event: ClientboundUpdateAttributesPacketEvent) {
        if (NMSUtils.getEntity(event.entityId) !is Player) return

        if (REMOVE_ATTRIBUTES_ON_CLIENTBOUND_UPDATE_ATTRIBUTES_PACKET.isNotEmpty()) {
            // 移除指定的属性

            val old = event.values
            val new = old.filterNot { snapshot ->
                REMOVE_ATTRIBUTES_ON_CLIENTBOUND_UPDATE_ATTRIBUTES_PACKET.any {
                    snapshot.attribute().`is`(it)
                }
            }
            event.values = new
        }
    }

    /**
     * 作用:
     *
     * - 修改 ClientboundSetEntityDataPacketEvent 中的黄心数量为 0 (为了让客户端不再显示)
     */
    @PacketHandler
    private fun handleClientboundSetEntityMetadata(event: ClientboundSetEntityDataPacketEvent) {
        if (NMSUtils.getEntity(event.id) !is Player) return

        if (REMOVE_ABSORPTION_ON_CLIENTBOUND_SET_ENTITY_DATA_PACKET) {
            // 修改黄心为 0

            val old = event.packedItems.toMutableList()
            val removed = old.removeIf { it.id == SHADOW_PLAYER.DATA_PLAYER_ABSORPTION_ID.id }
            if (!removed) return
            val new = old + SynchedEntityData.DataValue.create(SHADOW_PLAYER.DATA_PLAYER_ABSORPTION_ID, 0f)
            event.packedItems = new
        }
    }

}
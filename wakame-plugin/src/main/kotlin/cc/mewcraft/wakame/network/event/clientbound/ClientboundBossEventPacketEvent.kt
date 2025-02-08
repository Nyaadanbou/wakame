package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.ClientboundBossEventPacket
import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import cc.mewcraft.wakame.util.MutableLazy
import cc.mewcraft.wakame.util.bossbar.operation.BossBarOperation
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import org.bukkit.entity.Player
import java.lang.invoke.MethodHandles
import java.util.*

private val CLIENTBOUND_BOSS_EVENT_PACKET_ID_GETTER = MethodHandles
    .privateLookupIn(ClientboundBossEventPacket::class.java, MethodHandles.lookup())
    .findGetter(ClientboundBossEventPacket::class.java, "id", UUID::class.java)

class ClientboundBossEventPacketEvent(
    player: Player,
    packet: ClientboundBossEventPacket
) : PlayerPacketEvent<ClientboundBossEventPacket>(player, packet) {
    
    var id: UUID by MutableLazy({ changed = true }) { CLIENTBOUND_BOSS_EVENT_PACKET_ID_GETTER.invoke(packet) as UUID }
    var operation: BossBarOperation by MutableLazy({ changed = true }) { BossBarOperation.fromPacket(packet) }
    
    override fun buildChangedPacket(): ClientboundBossEventPacket {
        return ClientboundBossEventPacket(id, operation)
    }
    
}
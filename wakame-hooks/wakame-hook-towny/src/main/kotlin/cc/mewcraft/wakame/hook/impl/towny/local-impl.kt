package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.gui.towny.townyHookConfig
import cc.mewcraft.wakame.integration.towny.Government
import cc.mewcraft.wakame.integration.towny.TownyLocal
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.TownyObject
import com.palmergames.bukkit.towny.`object`.metadata.ByteDataField
import com.palmergames.bukkit.towny.`object`.metadata.LongDataField
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*
import cc.mewcraft.wakame.integration.towny.Nation as KoishNation
import cc.mewcraft.wakame.integration.towny.Town as KoishTown

private val townyApi: TownyAPI
    get() = TownyAPI.getInstance()

class TownyTownyLocal : TownyLocal {
    override fun getTowns(): Collection<KoishTown> {
        return townyApi.towns.map(::TownyTown)
    }

    override fun getNations(): Collection<KoishNation> {
        return townyApi.nations.map(::TownyNation)
    }

    override fun joinsMarketNetwork(government: Government) {
        val townyObject = (government as TownyHandle).handle
        val byteDataField = ByteDataField(EntryFilter.JoinedMarketNetwork.METADATA_KEY_JOINED_MARKET_NETWORK, 0)
        townyObject.addMetaData(byteDataField)
    }

    override fun leavesMarketNetwork(government: Government) {
        val townyObject = (government as TownyHandle).handle
        townyObject.removeMetaData(EntryFilter.JoinedMarketNetwork.METADATA_KEY_JOINED_MARKET_NETWORK)
    }

    override fun hasJoinedMarketNetwork(government: Government): Boolean {
        val townyObject = (government as TownyHandle).handle
        return townyObject.hasMeta(EntryFilter.JoinedMarketNetwork.METADATA_KEY_JOINED_MARKET_NETWORK, ByteDataField::class.java)
    }

    override fun paysMarketNetworkTax(government: Government) {
        val townyObject = (government as TownyHandle).handle
        val longDataField = LongDataField(EntryFilter.MarketNetworkTaxPeriod.METADATA_KEY_MARKET_NETWORK_TAX_PERIOD, System.currentTimeMillis())
        townyObject.addMetaData(longDataField)
    }

    override fun hasPaidMarketNetworkTax(government: Government): Boolean {
        val townyObject = (government as TownyHandle).handle
        return townyObject.hasMeta(EntryFilter.MarketNetworkTaxPeriod.METADATA_KEY_MARKET_NETWORK_TAX_PERIOD, LongDataField::class.java)
    }

    override fun isMayor(playerId: UUID): Boolean {
        val resident = townyApi.getResident(playerId) ?: return false
        return resident.isMayor
    }

    override fun isKing(playerId: UUID): Boolean {
        val resident = townyApi.getResident(playerId) ?: return false
        return resident.isKing
    }

    override fun getTown(playerId: UUID): KoishTown? {
        val resident = townyApi.getResident(playerId) ?: return null
        val town = resident.town ?: return null
        return TownyTown(town)
    }

    override fun getNation(playerId: UUID): KoishNation? {
        val resident = townyApi.getResident(playerId) ?: return null
        val nation = resident.nation ?: return null
        return TownyNation(nation)
    }
}

private sealed interface TownyHandle {
    val handle: TownyObject
}

private class TownyTown(
    override val handle: Town,
) : KoishTown, TownyHandle {

    companion object {
        private val entryFilter: List<EntryFilter> by townyHookConfig.entryOrElse(
            default = listOf(),
            path = arrayOf("town_list_menu", "entry_filter")
        )
    }

    override val name: Component
        get() = Component.text(handle.name)
    override val balance: Double
        get() = handle.account.holdingBalance
    override val canShow: Boolean // `all ` 逻辑
        get() = entryFilter.all { it.test(handle) }

    override fun withdraw(amount: Double) {
        handle.account.withdraw(amount, "Koish")
    }

    override fun deposit(amount: Double) {
        handle.account.deposit(amount, "Koish")
    }

    override fun teleport(player: Player) {
        val spawn = handle.spawnOrNull ?: return
        player.teleportAsync(spawn)
    }
}

private class TownyNation(
    override val handle: Nation,
) : KoishNation, TownyHandle {

    companion object {
        private val entryFilter: List<EntryFilter> by townyHookConfig.entryOrElse(
            default = listOf(),
            path = arrayOf("nation_list_menu", "entry_filter")
        )
    }

    override val name: Component
        get() = Component.text(handle.name)
    override val balance: Double
        get() = handle.account.holdingBalance
    override val canShow: Boolean
        get() = entryFilter.all { it.test(handle) }

    override fun withdraw(amount: Double) {
        handle.account.withdraw(amount, "Koish")
    }

    override fun deposit(amount: Double) {
        handle.account.deposit(amount, "Koish")
    }

    override fun teleport(player: Player) {
        val spawn = handle.spawnOrNull ?: return
        player.teleportAsync(spawn)
    }
}

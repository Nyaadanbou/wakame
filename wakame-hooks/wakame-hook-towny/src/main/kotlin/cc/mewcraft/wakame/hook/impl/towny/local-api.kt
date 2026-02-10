@file:JvmName("TownyLocal")

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
import com.palmergames.bukkit.towny.`object`.metadata.StringDataField
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

private sealed class AbstractGovernment<T : TownyObject>(
    val townyObject: T,
) : Government {

    // Market network operations (shared)
    override fun joinsMarketNetwork() {
        val byteDf = ByteDataField(EntryFilter.JoinedMarketNetwork.KEY_JOINED_MARKET_NETWORK, 0)
        townyObject.addMetaData(byteDf)
    }

    override fun leavesMarketNetwork() {
        townyObject.removeMetaData(EntryFilter.JoinedMarketNetwork.KEY_JOINED_MARKET_NETWORK)
    }

    override fun hasJoinedMarketNetwork(): Boolean {
        return townyObject.hasMeta(EntryFilter.JoinedMarketNetwork.KEY_JOINED_MARKET_NETWORK, ByteDataField::class.java)
    }

    override fun paysMarketNetworkTax() {
        val longDf = LongDataField(EntryFilter.MarketNetworkTaxPeriod.KEY_MARKET_NETWORK_TAX_PERIOD, System.currentTimeMillis())
        townyObject.addMetaData(longDf)
    }

    override fun hasPaidMarketNetworkTax(): Boolean {
        return townyObject.hasMeta(EntryFilter.MarketNetworkTaxPeriod.KEY_MARKET_NETWORK_TAX_PERIOD, LongDataField::class.java)
    }
}

private class TownyTown(
    townyObject: Town,
) : AbstractGovernment<Town>(townyObject), KoishTown {
    companion object {
        private const val KEY_GOVERNMENT_BOARD = "government_board"

        private val defaultBoard: List<String> by townyHookConfig.entryOrElse(
            default = listOf(),
            path = arrayOf("town_list_menu", "default_board")
        )

        private val entryFilter: List<EntryFilter> by townyHookConfig.entryOrElse(
            default = listOf(),
            path = arrayOf("town_list_menu", "entry_filter")
        )
    }

    override val name: Component
        get() = Component.text(townyObject.name)
    override var board: List<String>
        get() {
            val meta = townyObject.getMeta<StringDataField>(KEY_GOVERNMENT_BOARD) ?: return defaultBoard
            val texts = meta.value.split("\n")
            return texts
        }
        set(value) {
            val stringDf = StringDataField(KEY_GOVERNMENT_BOARD, value.joinToString("\n"))
            townyObject.addMetaData(stringDf)
        }
    override val balance: Double
        get() = townyObject.account.holdingBalance
    override val canShow: Boolean // `all ` 逻辑
        get() = entryFilter.all { it.test(townyObject) }

    override fun withdraw(amount: Double) {
        townyObject.account.withdraw(amount, "Koish")
    }

    override fun deposit(amount: Double) {
        townyObject.account.deposit(amount, "Koish")
    }

    override fun teleport(player: Player) {
        val spawn = townyObject.spawnOrNull ?: return
        player.teleportAsync(spawn)
    }
}

private class TownyNation(
    townyObject: Nation,
) : AbstractGovernment<Nation>(townyObject), KoishNation {
    companion object {
        private const val KEY_GOVERNMENT_BOARD = "government_board"

        private val defaultBoard: List<String> by townyHookConfig.entryOrElse(
            default = listOf(),
            path = arrayOf("town_list_menu", "default_board")
        )

        private val entryFilter: List<EntryFilter> by townyHookConfig.entryOrElse(
            default = listOf(),
            path = arrayOf("nation_list_menu", "entry_filter")
        )
    }

    override val name: Component
        get() = Component.text(townyObject.name)
    override var board: List<String>
        get() {
            val meta = townyObject.getMeta<StringDataField>(KEY_GOVERNMENT_BOARD) ?: return defaultBoard
            val texts = meta.value.split("\n")
            return texts
        }
        set(value) {
            val stringDf = StringDataField(KEY_GOVERNMENT_BOARD, value.joinToString("\n"))
            townyObject.addMetaData(stringDf)
        }
    override val balance: Double
        get() = townyObject.account.holdingBalance
    override val canShow: Boolean
        get() = entryFilter.all { it.test(townyObject) }

    override fun withdraw(amount: Double) {
        townyObject.account.withdraw(amount, "Koish")
    }

    override fun deposit(amount: Double) {
        townyObject.account.deposit(amount, "Koish")
    }

    override fun teleport(player: Player) {
        val spawn = townyObject.spawnOrNull ?: return
        player.teleportAsync(spawn)
    }
}

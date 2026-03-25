@file:JvmName("TownyBridgeLocal")

package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.integration.townybridgelocal.*
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownyObject
import com.palmergames.bukkit.towny.`object`.metadata.ByteDataField
import com.palmergames.bukkit.towny.`object`.metadata.StringDataField
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*
import com.palmergames.bukkit.towny.`object`.Government as TownyGovernment
import com.palmergames.bukkit.towny.`object`.Nation as TownyNation
import com.palmergames.bukkit.towny.`object`.Town as TownyTown

class TownyLocalBridgeImpl : TownyLocalBridge {
    override fun getTowns(): Collection<Town> {
        return TownyAPI.getInstance().towns.map(::TownImpl)
    }

    override fun getNations(): Collection<Nation> {
        return TownyAPI.getInstance().nations.map(::NationImpl)
    }

    override fun isMayor(playerId: UUID): Boolean {
        val resident = TownyAPI.getInstance().getResident(playerId) ?: return false
        return resident.isMayor
    }

    override fun isKing(playerId: UUID): Boolean {
        val resident = TownyAPI.getInstance().getResident(playerId) ?: return false
        return resident.isKing
    }

    override fun getTown(playerId: UUID): Town? {
        val resident = TownyAPI.getInstance().getResident(playerId) ?: return null
        val town = resident.town ?: return null
        return TownImpl(town)
    }

    override fun getNation(playerId: UUID): Nation? {
        val resident = TownyAPI.getInstance().getResident(playerId) ?: return null
        val nation = resident.nation ?: return null
        return NationImpl(nation)
    }
}

interface GovernmentLike<T : TownyGovernment> {
    val government: T
}

internal class GovernmentComponent(
    val government: TownyGovernment,
) : Government {
    override val balance: Double
        get() = government.account.holdingBalance

    override fun withdraw(amount: Double) {
        government.account.withdraw(amount, "Koish")
    }

    override fun deposit(amount: Double) {
        government.account.deposit(amount, "Koish")
    }
}

internal class MenuListEntryComponent(
    private val government: TownyGovernment,
    private val defaultBoard: List<String>,
    private val entryFilter: List<EntryFilter>,
) : MenuListEntry {
    companion object {
        private const val KEY_GOVERNMENT_BOARD = "government_board"
    }

    override val name: Component
        get() = Component.text(government.name)

    override var board: List<String>
        get() {
            val stringDf = government.getMeta<StringDataField>(KEY_GOVERNMENT_BOARD) ?: return defaultBoard
            val strings = stringDf.value.split("\n")
            return strings
        }
        set(value) {
            val stringDf = StringDataField(KEY_GOVERNMENT_BOARD, value.joinToString("\n"))
            government.addMetaData(stringDf)
        }

    override val canShow: Boolean
        get() = entryFilter.all { it.test(government) }

    override fun teleport(player: Player) {
        val spawn = government.spawnOrNull ?: return
        player.teleportAsync(spawn)
    }
}

internal class MarketNetworkEntityComponent(
    private val townyObject: TownyObject,
) : MarketNetworkEntity {
    companion object {
        const val KEY_JOINED_MARKET_NETWORK = "joined_market_network"
    }

    override fun joinsMarketNetwork() {
        val byteDf = ByteDataField(KEY_JOINED_MARKET_NETWORK, 0)
        townyObject.addMetaData(byteDf)
    }

    override fun leavesMarketNetwork() {
        townyObject.removeMetaData(KEY_JOINED_MARKET_NETWORK)
    }

    override fun hasJoinedMarketNetwork(): Boolean {
        return townyObject.hasMeta(KEY_JOINED_MARKET_NETWORK, ByteDataField::class.java)
    }
}

private class TownImpl(
    override val government: TownyTown,
) : Town,
    GovernmentLike<TownyTown>,
    Government by GovernmentComponent(government),
    MenuListEntry by MenuListEntryComponent(government, defaultBoard, entryFilter),
    MarketNetworkEntity by MarketNetworkEntityComponent(government) {
    companion object {
        private val defaultBoard: List<String> by TOWNY_HOOK_CONFIG.entryOrElse(listOf(), "town_list_menu", "default_board")
        private val entryFilter: List<EntryFilter> by TOWNY_HOOK_CONFIG.entryOrElse(listOf(), "town_list_menu", "entry_filter")
    }
}

private class NationImpl(
    override val government: TownyNation,
) : Nation,
    GovernmentLike<TownyNation>,
    Government by GovernmentComponent(government),
    MenuListEntry by MenuListEntryComponent(government, defaultBoard, entryFilter),
    MarketNetworkEntity by MarketNetworkEntityComponent(government) {
    companion object {
        private val defaultBoard: List<String> by TOWNY_HOOK_CONFIG.entryOrElse(listOf(), "nation_list_menu", "default_board")
        private val entryFilter: List<EntryFilter> by TOWNY_HOOK_CONFIG.entryOrElse(listOf(), "nation_list_menu", "entry_filter")
    }
}

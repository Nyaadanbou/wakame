package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import com.palmergames.bukkit.towny.`object`.Government
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.TownyObject
import com.palmergames.bukkit.towny.`object`.metadata.ByteDataField
import com.palmergames.bukkit.towny.`object`.metadata.LongDataField
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.time.Duration

interface EntryFilter {

    companion object {
        fun serializer(): TypeSerializerCollection = TypeSerializerCollection.builder()
            .registerExact(
                DispatchingSerializer.createPartial(
                    mapOf(
                        "public" to Public::class,
                        "bankrupt" to Bankrupt::class,
                        "joined_market_network" to JoinedMarketNetwork::class,
                        "market_network_tax_period" to MarketNetworkTaxPeriod::class,
                    )
                )
            )
            .build()
    }

    fun test(townyObject: TownyObject): Boolean

    @ConfigSerializable
    data class Bankrupt(
        val invert: Boolean = false,
    ) : EntryFilter {
        override fun test(townyObject: TownyObject): Boolean {
            val bool = townyObject is Town && townyObject.isBankrupt
            return invert xor bool
        }
    }

    @ConfigSerializable
    data class Public(
        val invert: Boolean = false,
    ) : EntryFilter {
        override fun test(townyObject: TownyObject): Boolean {
            val bool = townyObject is Government && townyObject.isPublic
            return invert xor bool
        }
    }

    @ConfigSerializable
    data class JoinedMarketNetwork(
        val invert: Boolean = false,
    ) : EntryFilter {
        companion object {
            const val KEY_JOINED_MARKET_NETWORK = "joined_market_network"
        }

        override fun test(townyObject: TownyObject): Boolean {
            val bool = townyObject.hasMeta<ByteDataField>(KEY_JOINED_MARKET_NETWORK)
            return invert xor bool
        }
    }

    @ConfigSerializable
    data class MarketNetworkTaxPeriod(
        val period: Duration,
        val invert: Boolean = false,
    ) : EntryFilter {
        companion object {
            const val KEY_MARKET_NETWORK_TAX_PERIOD = "market_network_tax_period"
        }

        override fun test(townyObject: TownyObject): Boolean {
            val meta = townyObject.getMeta<LongDataField>(KEY_MARKET_NETWORK_TAX_PERIOD) ?: return false
            val bool = System.currentTimeMillis() - meta.value <= period.toMillis()
            return invert xor bool
        }
    }
}
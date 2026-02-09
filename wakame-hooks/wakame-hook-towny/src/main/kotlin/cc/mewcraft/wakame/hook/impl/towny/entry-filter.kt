package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
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
                        "joined_market_network" to JoinedMarketNetwork::class,
                        "market_network_tax_period" to MarketNetworkTaxPeriod::class,
                    )
                )
            )
            .register(JoinedMarketNetwork.serializer())
            .build()
    }

    fun test(townyObject: TownyObject): Boolean

    class JoinedMarketNetwork : EntryFilter {
        companion object {
            const val METADATA_KEY_JOINED_MARKET_NETWORK = "joined_market_network"

            fun serializer(): SimpleSerializer<JoinedMarketNetwork> =
                SimpleSerializer { node, obj -> JoinedMarketNetwork() }
        }

        override fun test(townyObject: TownyObject): Boolean {
            return townyObject.hasMeta<ByteDataField>(METADATA_KEY_JOINED_MARKET_NETWORK)
        }
    }

    @ConfigSerializable
    data class MarketNetworkTaxPeriod(
        val period: Duration,
    ) : EntryFilter {
        companion object {
            const val METADATA_KEY_MARKET_NETWORK_TAX_PERIOD = "market_network_tax_period"
        }

        override fun test(townyObject: TownyObject): Boolean {
            val meta = townyObject.getMeta<LongDataField>(METADATA_KEY_MARKET_NETWORK_TAX_PERIOD) ?: return false
            return System.currentTimeMillis() - meta.value <= period.toMillis()
        }
    }
}
package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import com.palmergames.bukkit.towny.`object`.Government as TownyGovernment
import com.palmergames.bukkit.towny.`object`.Town as TownyTown

interface EntryFilter {

    companion object {
        fun serializer(): TypeSerializerCollection = TypeSerializerCollection.builder()
            .registerExact(
                DispatchingSerializer.createPartial(
                    mapOf(
                        "public" to Public::class,
                        "bankrupt" to Bankrupt::class,
                        "joined_market_network" to JoinedMarketNetwork::class,
                    )
                )
            )
            .build()
    }

    fun test(government: TownyGovernment): Boolean

    @ConfigSerializable
    data class Bankrupt(
        val invert: Boolean = false,
    ) : EntryFilter {
        override fun test(government: TownyGovernment): Boolean {
            val town = (government as? TownyTown) ?: return false
            return invert xor town.isBankrupt
        }
    }

    @ConfigSerializable
    data class Public(
        val invert: Boolean = false,
    ) : EntryFilter {
        override fun test(government: TownyGovernment): Boolean {
            return invert xor government.isPublic
        }
    }

    @ConfigSerializable
    data class JoinedMarketNetwork(
        val invert: Boolean = false,
    ) : EntryFilter {
        override fun test(government: TownyGovernment): Boolean {
            val marketNetworkEntity = MarketNetworkEntityComponent(government)
            return invert xor marketNetworkEntity.hasJoinedMarketNetwork()
        }
    }
}
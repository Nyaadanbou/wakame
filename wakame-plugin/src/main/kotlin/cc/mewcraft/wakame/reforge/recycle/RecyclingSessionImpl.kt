package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.economy.EconomyIntegration2
import cc.mewcraft.wakame.item.koishTypeId
import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.util.decorate
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import java.util.stream.Stream
import kotlin.random.Random

/**
 * [RecyclingSession] 的一般实现.
 *
 * @param station
 * @param viewer
 * @param maxClaims 最大可同时回收的物品数量
 */
internal class SimpleRecyclingSession(
    override val station: RecyclingStation,
    override val viewer: Player,
    private val maxClaims: Int,
) : RecyclingSession {
    val logger: Logger = LOGGER.decorate(prefix = ReforgingStationConstants.RECYCLING_LOG_PREFIX)

    private val claims: ArrayList<Claim> = ArrayList(maxClaims)

    private fun getItemKey(item: ItemStack): Key {
        return item.koishTypeId ?: item.type.key
    }

    private fun getItemPrice(item: ItemStack?): PriceInstance? {
        if (item == null) {
            return null
        }
        val key = getItemKey(item)
        return station.getPrice(key)
    }

    override fun claimItem(item: ItemStack, playerSlot: Int): RecyclingSession.ClaimResult {
        if (claims.size >= maxClaims) {
            return ClaimResult.failure(
                RecyclingSession.ClaimResult.Failure.Reason.TOO_MANY_CLAIMS
            )
        }

        val price = getItemPrice(item)
        if (price == null) {
            return ClaimResult.failure(
                RecyclingSession.ClaimResult.Failure.Reason.UNSUPPORTED_ITEM
            )
        }

        claims += Claim(playerSlot, item, price)

        // logger.info("${viewer.name} added a claim!")

        return ClaimResult.success(claims.lastIndex + 1)
    }

    override fun purchase(dryRun: Boolean): RecyclingSession.PurchaseResult {
        if (claims.isEmpty()) {
            // logger.info("Purchase result: (empty).")
            return PurchaseResult.empty()
        }

        var totalMin = .0
        var totalMax = .0
        claims.forEach { claim ->
            val originalItem = claim.originalItem
            val priceInstance = claim.priceInstance
            val minimumPrice = priceInstance.getMinimumValue(originalItem)
            val maximumPrice = priceInstance.getMaximumValue(originalItem)
            totalMin += minimumPrice
            totalMax += maximumPrice
        }
        val totalPoint =
            if (totalMin < totalMax) {
                Random.nextDouble()
                Random.nextDouble(totalMin, totalMax)
            } else {
                totalMin
            }

        if (!dryRun) {
            claims.clear()
            claims.forEach { claim ->
                claim.originalItem.amount = 0
            }

            // logger.info("Sold for $totalPoint in total.")

            val result = EconomyIntegration2.give(viewer.uniqueId, totalPoint)
            if (result.isFailure) {
                logger.error("Failed to give money to ${viewer.name}.")
                return PurchaseResult.failure(
                    RecyclingSession.PurchaseResult.Failure.Reason.UNKNOWN
                )
            }
        }

        // logger.info("Purchase result: (totalMin=$totalMin, totalMax=$totalMax, totalPoint=$totalPoint)")
        return PurchaseResult.success(totalMin, totalMax, totalPoint)
    }

    override fun getClaim(displaySlot: Int): RecyclingSession.Claim? {
        if (displaySlot < 0 || displaySlot >= claims.size) {
            return null
        }
        return claims[displaySlot]
    }

    override fun removeClaim(displaySlot: Int): RecyclingSession.Claim? {
        val index = claims.indexOfFirst { claim -> claim.displaySlot == displaySlot }
        if (index < 0) {
            return null
        }
        return claims.removeAt(index)
    }

    override fun reset() {
        claims.clear()
    }

    override fun getAllClaims(): Collection<RecyclingSession.Claim> {
        return claims
    }

    override fun getAllInputs(): Array<ItemStack> {
        return claims.map { it.originalItem }.toTypedArray()
    }


    ////// inner classes //////


    private inner class Claim(
        override val playerSlot: Int,
        override val originalItem: ItemStack,
        override val priceInstance: PriceInstance,
    ) : RecyclingSession.Claim, Examinable {
        override val displaySlot: Int
            get() = claims.indexOf(this)

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("displaySlot", displaySlot),
                ExaminableProperty.of("playerSlot", playerSlot),
            )
        }

        override fun toString(): String {
            return super.toString()
        }
    }

    private object ClaimResult {
        fun failure(
            reason: RecyclingSession.ClaimResult.Failure.Reason,
        ): Failure {
            return Failure(reason)
        }

        fun success(
            displaySlot: Int,
        ): Success {
            return Success(displaySlot)
        }

        data class Failure(
            override val reason: RecyclingSession.ClaimResult.Failure.Reason,
        ) : RecyclingSession.ClaimResult.Failure

        data class Success(
            override val displaySlot: Int,
        ) : RecyclingSession.ClaimResult.Success
    }

    private object PurchaseResult {
        fun empty(): Empty {
            return Empty
        }

        fun failure(
            reason: RecyclingSession.PurchaseResult.Failure.Reason,
        ): Failure {
            return Failure(reason)
        }

        fun success(
            minPrice: Double,
            maxPrice: Double,
            fixPrice: Double,
        ): Success {
            return Success(minPrice, maxPrice, fixPrice)
        }

        data object Empty : RecyclingSession.PurchaseResult.Empty

        data class Failure(
            override val reason: RecyclingSession.PurchaseResult.Failure.Reason,
        ) : RecyclingSession.PurchaseResult.Failure

        data class Success(
            override val minPrice: Double,
            override val maxPrice: Double,
            override val fixPrice: Double,
        ) : RecyclingSession.PurchaseResult.Success
    }
}
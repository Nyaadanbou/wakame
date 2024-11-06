package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.util.decorate
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import java.util.stream.Stream
import kotlin.random.Random

internal class SimpleRecyclingSession(
    override val station: RecyclingStation,
    override val viewer: Player,
    private val maxClaims: Int,
) : RecyclingSession, KoinComponent {
    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.RECYCLE)

    private val claims: ArrayList<Claim> = ArrayList(maxClaims)

    override fun claimItem(item: ItemStack, playerSlot: Int): RecyclingSession.ClaimResult {
        if (claims.size >= maxClaims) {
            return ClaimResult.failure(
                RecyclingSession.ClaimResult.Failure.Reason.TOO_MANY_CLAIMS
            )
        }

        val displaySlot = claims.lastIndex + 1
        val claim = Claim(playerSlot, item)
        claims += claim

        logger.info("${viewer.name} added a claim!")

        return ClaimResult.success(displaySlot)
    }

    override fun purchase(dryRun: Boolean): RecyclingSession.PurchaseResult {
        if (claims.isEmpty()) {
            logger.info("Purchase result: (empty).")
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

            logger.info("Sold for $totalPoint in total.")
            // TODO #227 真的给玩家转账
        }

        logger.info("Purchase result: (totalMin=$totalMin, totalMax=$totalMax, totalPoint=$totalPoint)")
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

    override fun hasAnyClaims(): Boolean {
        return claims.isNotEmpty()
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
    ) : RecyclingSession.Claim, Examinable {
        override val displaySlot: Int
            get() = claims.indexOf(this)

        // TODO #227 从配置文件读取 PriceInstance
        override val priceInstance: PriceInstance = PriceInstance(10.0, 15.0, mapOf())

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
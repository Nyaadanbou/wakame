package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.util.decorate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
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
                text("Too many claims!").color(NamedTextColor.RED)
            )
        }

        val displaySlot = claims.lastIndex + 1
        val claim = Claim(playerSlot, item)
        claims += claim

        logger.info("${viewer.name} claimed a recycling!")

        return ClaimResult.success(
            text("Claimed!"), displaySlot
        )
    }

    override fun purchase(dryRun: Boolean): RecyclingSession.PurchaseResult {
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

            // TODO #227 真的给玩家转账
        }

        logger.info("Sold for $totalPoint")

        return PurchaseResult.success(
            text("Sold for ${totalPoint}!"), totalMin, totalMax, totalPoint
        )
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
        fun failure(description: List<Component>): Failure {
            return Failure(description)
        }

        fun failure(description: Component): Failure {
            return Failure(listOf(description))
        }

        fun success(description: List<Component>, displaySlot: Int): Success {
            return Success(description, displaySlot)
        }

        fun success(description: Component, displaySlot: Int): Success {
            return Success(listOf(description), displaySlot)
        }

        data class Failure(
            override val description: List<Component>,
        ) : RecyclingSession.ClaimResult.Failure

        data class Success(
            override val description: List<Component>,
            override val displaySlot: Int,
        ) : RecyclingSession.ClaimResult.Success
    }

    private object PurchaseResult {
        fun failure(description: List<Component>): Failure {
            return Failure(description)
        }

        fun failure(description: Component): Failure {
            return Failure(listOf(description))
        }

        fun success(
            description: List<Component>,
            minPrice: Double,
            maxPrice: Double,
            fixPrice: Double,
        ): Success {
            return Success(description, minPrice, maxPrice, fixPrice)
        }

        fun success(
            description: Component,
            minPrice: Double,
            maxPrice: Double,
            fixPrice: Double,
        ): Success {
            return Success(listOf(description), minPrice, maxPrice, fixPrice)
        }

        data class Failure(
            override val description: List<Component>,
        ) : RecyclingSession.PurchaseResult.Failure

        data class Success(
            override val description: List<Component>,
            override val minPrice: Double,
            override val maxPrice: Double,
            override val fixPrice: Double,
        ) : RecyclingSession.PurchaseResult.Success
    }
}
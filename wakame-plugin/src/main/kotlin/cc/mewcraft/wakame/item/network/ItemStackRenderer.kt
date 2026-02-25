package cc.mewcraft.wakame.item.network

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.optionalEntry
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.display.ItemRenderers
import cc.mewcraft.wakame.item.isNetworkRewrite
import cc.mewcraft.wakame.item.koishTypeId
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.mixin.support.KoishDataSanitizer
import cc.mewcraft.wakame.network.event.PacketHandler
import cc.mewcraft.wakame.network.event.PacketListener
import cc.mewcraft.wakame.network.event.clientbound.*
import cc.mewcraft.wakame.network.event.registerPacketListener
import cc.mewcraft.wakame.network.event.unregisterPacketListener
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.getOrThrow
import com.mojang.datafixers.util.Pair
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.HoverEvent
import net.minecraft.core.component.DataComponentExactPredicate
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.display.*
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.GameMode
import org.bukkit.entity.Player
import xyz.xenondevs.commons.provider.orElse
import java.util.*

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "renderer").orElse(false)

/**
 * 修改 [net.minecraft.world.item.ItemStack].
 */
@Init(InitStage.POST_WORLD)
object ItemStackRenderer : PacketListener {

    @InitFun
    internal fun init() {
        registerPacketListener()
    }

    @DisableFun
    internal fun disable() {
        unregisterPacketListener()
    }

    @PacketHandler
    private fun handleContainerSetContent(event: ClientboundContainerSetContentPacketEvent) {
        val player = event.player
        if (isCreative(player)) return
        val items = event.items
        items.forEachIndexed { i, item ->
            items[i] = item.copy().modify(player)
        }
        event.carriedItem = event.carriedItem.copy().modify(player)
    }

    @PacketHandler
    private fun handleContainerSetSlot(event: ClientboundContainerSetSlotPacketEvent) {
        val player = event.player
        if (isCreative(player)) return
        event.item = event.item.copy().modify(player)
    }

    @PacketHandler
    private fun handleSetEntityData(event: ClientboundSetEntityDataPacketEvent) {
        val player = event.player
        val oldItems = event.packedItems
        val newItems = ArrayList<DataValue<*>>()
        for (dataValue in oldItems) {
            val value = dataValue.value
            if (value is MojangStack) {
                newItems += DataValue(
                    dataValue.id,
                    EntityDataSerializers.ITEM_STACK,
                    value.copy().modify(player)
                )
            } else {
                newItems += dataValue
            }
        }
        event.packedItems = newItems
    }

    @PacketHandler
    private fun handleSetEquipment(event: ClientboundSetEquipmentPacketEvent) {
        val player = event.player
        if (isCreative(player)) return
        val slots = ArrayList(event.slots).also { event.slots = it }
        for ((i, pair) in slots.withIndex()) {
            slots[i] = Pair(
                pair.first,
                pair.second.copy().modify(player)
            )
        }
    }

    @PacketHandler
    private fun handleMerchantOffers(event: ClientboundMerchantOffersPacketEvent) {
        val player = event.player
        val newOffers = MerchantOffers()
        event.offers.forEach { offer ->
            val stackA = offer.baseCostA.itemStack.copy().modify(player)
            val costA = ItemCost(stackA.itemHolder, stackA.count, DataComponentExactPredicate.EMPTY, stackA)
            val costB = offer.costB.map {
                val stackB = it.itemStack.copy().modify(player)
                ItemCost(stackB.itemHolder, stackB.count, DataComponentExactPredicate.EMPTY, stackB)
            }
            newOffers += MerchantOffer(
                costA, costB, offer.result.copy().modify(player),
                offer.uses, offer.maxUses, offer.xp, offer.priceMultiplier, offer.demand
            )
        }
        event.offers = newOffers
    }

    @PacketHandler
    private fun handleRecipeBookAdd(event: ClientboundRecipeBookAddPacketEvent) {
        val player = event.player
        event.entries = event.entries.map { entry ->
            val contents = entry.contents
            ClientboundRecipeBookAddPacket.Entry(
                RecipeDisplayEntry(
                    contents.id,
                    modifyRecipeDisplay(player, contents.display),
                    contents.group,
                    contents.category,
                    modifyIngredientList(player, contents.craftingRequirements)
                ),
                entry.notification(),
                entry.highlight()
            )
        }
    }

    @PacketHandler
    private fun handlePlaceGhostRecipe(event: ClientboundPlaceGhostRecipePacketEvent) {
        val player = event.player
        event.recipeDisplay = modifyRecipeDisplay(player, event.recipeDisplay)
    }

    @PacketHandler
    private fun handleSystemChat(event: ClientboundSystemChatPacketEvent) {
        val player = event.player
        event.message = renderShowItem(player, event.message)
    }

    @PacketHandler
    private fun handlePlayerCombatKill(event: ClientboundPlayerCombatKillPacketEvent) {
        val player = event.player
        event.message = renderShowItem(player, event.message)
    }

    private fun modifyIngredientList(player: Player, optList: Optional<List<Ingredient>>): Optional<List<Ingredient>> =
        optList.map { ingredientList ->
            ingredientList.map { ingredient ->
                val itemStacks = ingredient.itemStacks()
                if (itemStacks != null)
                    Ingredient.ofStacks(itemStacks.map { it.copy().modify(player) })
                else ingredient
            }
        }

    private fun modifyRecipeDisplay(player: Player, display: RecipeDisplay): RecipeDisplay = when (display) {
        is FurnaceRecipeDisplay -> FurnaceRecipeDisplay(
            modifySlotDisplay(player, display.ingredient),
            modifySlotDisplay(player, display.fuel),
            modifySlotDisplay(player, display.result),
            modifySlotDisplay(player, display.craftingStation),
            display.duration,
            display.experience
        )

        is ShapedCraftingRecipeDisplay -> ShapedCraftingRecipeDisplay(
            display.width, display.height,
            display.ingredients.map { display -> modifySlotDisplay(player, display) },
            modifySlotDisplay(player, display.result),
            modifySlotDisplay(player, display.craftingStation)
        )

        is ShapelessCraftingRecipeDisplay -> ShapelessCraftingRecipeDisplay(
            display.ingredients.map { display -> modifySlotDisplay(player, display) },
            modifySlotDisplay(player, display.result),
            modifySlotDisplay(player, display.craftingStation)
        )

        is SmithingRecipeDisplay -> SmithingRecipeDisplay(
            modifySlotDisplay(player, display.template),
            modifySlotDisplay(player, display.base),
            modifySlotDisplay(player, display.addition),
            modifySlotDisplay(player, display.result),
            modifySlotDisplay(player, display.craftingStation)
        )

        is StonecutterRecipeDisplay -> StonecutterRecipeDisplay(
            modifySlotDisplay(player, display.input),
            modifySlotDisplay(player, display.result),
            modifySlotDisplay(player, display.craftingStation)
        )

        else -> {
            LOGGER.warn("Unknown recipe display type: ${display.javaClass}")
            display
        }
    }

    private fun modifySlotDisplay(player: Player, display: SlotDisplay): SlotDisplay = when (display) {
        is SlotDisplay.Composite -> SlotDisplay.Composite(
            display.contents.map { display -> modifySlotDisplay(player, display) }
        )

        is SlotDisplay.ItemStackSlotDisplay -> SlotDisplay.ItemStackSlotDisplay(
            display.stack.copy().modify(player)
        )

        is SlotDisplay.SmithingTrimDemoSlotDisplay -> SlotDisplay.SmithingTrimDemoSlotDisplay(
            modifySlotDisplay(player, display.base),
            modifySlotDisplay(player, display.material),
            display.pattern
        )

        is SlotDisplay.WithRemainder -> SlotDisplay.WithRemainder(
            modifySlotDisplay(player, display.input),
            modifySlotDisplay(player, display.remainder)
        )

        is SlotDisplay.AnyFuel,
        is SlotDisplay.Empty,
        is SlotDisplay.ItemSlotDisplay,
        is SlotDisplay.TagSlotDisplay,
            -> display

        else -> {
            LOGGER.warn("Unknown slot display type: ${display.javaClass}")
            display
        }
    }

    /**
     * 渲染 [component] 中的物品堆叠.
     *
     * @param component 文本组件
     * @return 渲染后的 [component]
     */
    fun renderShowItem(player: Player?, component: Component): Component {
        val modified = renderShowItem0(player, component)
        if (modified is TranslatableComponent) {
            val modifiedChildren = modified.children().map { renderShowItem(player, it) }
            val modifiedArguments = modified.arguments().map { renderShowItem(player, it.asComponent()) }
            return modified.children(modifiedChildren).arguments(modifiedArguments)
        } else {
            val modifiedChildren = modified.children().map { renderShowItem(player, it) }
            return modified.children(modifiedChildren)
        }
    }

    private fun renderShowItem0(player: Player?, component: Component): Component {
        val hoverEvent = component.hoverEvent() ?: return component
        if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
            val itemstackInfo = hoverEvent.value() as HoverEvent.ShowItem
            val originItemstack = MojangStack(Registries.ITEM.getOrThrow(itemstackInfo.item()), itemstackInfo.count(), PaperAdventure.asVanilla(itemstackInfo.dataComponents()))
            val changedItemStack = originItemstack.copy().modify(player)
            KoishDataSanitizer.sanitizeItemStack(changedItemStack) // 清理 Koish 数据组件
            val changedHoverEvent = changedItemStack.asBukkitMirror().asHoverEvent()
            return component.hoverEvent(changedHoverEvent)
        }
        return component
    }

    private fun isCreative(player: Player): Boolean {
        // 创造模式会 1:1 复制它接收到的物品到客户端本地,
        // 而我们发给客户端的 Koish 物品并不是原始物品, 而是修改过的.
        // 问题在于, 修改过的 Koish 物品并不包含任何 Koish 数据组件,
        // 这也就导致了创造模式会让物品栏内的 Koish 物品失效.
        //
        // 这个问题的解决办法就是在物品上永远存一份原始数据的备份,
        // 但那样会导致额外的内存和性能开销. 不如等 Mojang 更新.
        //
        // 因此, 我们现阶段能做的就是忽略该问题.

        return player.gameMode === GameMode.CREATIVE
    }

    private fun MojangStack.modify(player: Player?): MojangStack {
        if (!isNetworkRewrite) return this
        try {
            ItemRenderers.STANDARD.render(this, player)
        } catch (e: Throwable) {
            if (LOGGING) {
                LOGGER.error("An error occurred while rewrite network item: ${this.koishTypeId}", e)
            }
        }
        return this
    }
}

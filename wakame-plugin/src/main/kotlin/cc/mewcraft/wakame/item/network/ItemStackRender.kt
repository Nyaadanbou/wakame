package cc.mewcraft.wakame.item.network

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.item.display.ItemRenderers
import cc.mewcraft.wakame.item.isNetworkRewrite
import cc.mewcraft.wakame.item.koishTypeId
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.network.event.*
import cc.mewcraft.wakame.network.event.clientbound.*
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.getOrThrow
import cc.mewcraft.wakame.util.item.toNMS
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import com.mojang.datafixers.util.Pair
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.event.player.AsyncChatEvent
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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import xyz.xenondevs.commons.provider.orElse
import java.util.*

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "renderer").orElse(false)

/**
 * 修改 [net.minecraft.world.item.ItemStack].
 */
@Init(InitStage.POST_WORLD)
internal object ItemStackRender : PacketListener, Listener {

    @InitFun
    fun init() {
        registerEvents()
        registerPacketListener()
    }

    @DisableFun
    fun disable() {
        unregisterEvents()
        unregisterPacketListener()
    }

    @PacketHandler
    private fun handleSetContentPacket(event: ClientboundContainerSetContentPacketEvent) {
        if (isCreative(event)) return
        val items = event.items

        items.forEachIndexed { i, item ->
            items[i] = item.modify()
        }

        event.carriedItem = event.carriedItem.modify()
    }

    @PacketHandler
    private fun handleSetSlot(event: ClientboundContainerSetSlotPacketEvent) {
        if (isCreative(event)) return
        event.item = event.item.modify()
    }

    @PacketHandler
    private fun handleEntityData(event: ClientboundSetEntityDataPacketEvent) {
        val oldItems = event.packedItems
        val newItems = ArrayList<DataValue<*>>()
        for (dataValue in oldItems) {
            val value = dataValue.value
            if (value is MojangStack) {
                newItems += DataValue(
                    dataValue.id,
                    EntityDataSerializers.ITEM_STACK,
                    value.modify()
                )
            } else {
                newItems += dataValue
            }
        }
        event.packedItems = newItems
    }

    @PacketHandler
    private fun handleSetEquipment(event: ClientboundSetEquipmentPacketEvent) {
        if (isCreative(event)) return
        val slots = ArrayList(event.slots).also { event.slots = it }

        for ((i, pair) in slots.withIndex()) {
            slots[i] = Pair(
                pair.first,
                pair.second.modify()
            )
        }
    }

    @PacketHandler
    private fun handleMerchantOffers(event: ClientboundMerchantOffersPacketEvent) {
        val newOffers = MerchantOffers()

        event.offers.forEach { offer ->
            val stackA = offer.baseCostA.itemStack.modify()
            val costA = ItemCost(stackA.itemHolder, stackA.count, DataComponentExactPredicate.EMPTY, stackA)
            val costB = offer.costB.map {
                val stackB = it.itemStack.modify()
                ItemCost(stackB.itemHolder, stackB.count, DataComponentExactPredicate.EMPTY, stackB)
            }
            newOffers += MerchantOffer(
                costA, costB, offer.result.modify(),
                offer.uses, offer.maxUses, offer.xp, offer.priceMultiplier, offer.demand
            )
        }

        event.offers = newOffers
    }

    @PacketHandler
    private fun handleRecipeBookAdd(event: ClientboundRecipeBookAddPacketEvent) {
        event.entries = event.entries.map { entry ->
            val contents = entry.contents
            ClientboundRecipeBookAddPacket.Entry(
                RecipeDisplayEntry(
                    contents.id,
                    modifyRecipeDisplay(contents.display),
                    contents.group,
                    contents.category,
                    modifyIngredientList(contents.craftingRequirements)
                ),
                entry.notification(),
                entry.highlight()
            )
        }
    }

    @PacketHandler
    private fun handlePlaceGhostRecipe(event: ClientboundPlaceGhostRecipePacketEvent) {
        event.recipeDisplay = modifyRecipeDisplay(event.recipeDisplay)
    }

    @EventHandler
    private fun handlePlayerChat(event: AsyncChatEvent) {
        val originMessage = event.message()
        event.message(modifyComponent(originMessage))
    }

    @PacketHandler
    private fun handleSystemChat(event: ClientboundSystemChatPacketEvent) {
        event.message = modifyComponent(event.message)
    }

    @PacketHandler
    private fun handleCombatKill(event: ClientboundPlayerCombatKillPacketEvent) {
        event.message = modifyComponent(event.message)
    }

    private fun modifyIngredientList(optList: Optional<List<Ingredient>>): Optional<List<Ingredient>> =
        optList.map { ingredientList ->
            ingredientList.map { ingredient ->
                val itemStacks = ingredient.itemStacks()
                if (itemStacks != null)
                    Ingredient.ofStacks(itemStacks.map { it.modify() })
                else ingredient
            }
        }

    private fun modifyRecipeDisplay(display: RecipeDisplay): RecipeDisplay = when (display) {
        is FurnaceRecipeDisplay -> FurnaceRecipeDisplay(
            modifySlotDisplay(display.ingredient),
            modifySlotDisplay(display.fuel),
            modifySlotDisplay(display.result),
            modifySlotDisplay(display.craftingStation),
            display.duration,
            display.experience
        )

        is ShapedCraftingRecipeDisplay -> ShapedCraftingRecipeDisplay(
            display.width, display.height,
            display.ingredients.map(::modifySlotDisplay),
            modifySlotDisplay(display.result),
            modifySlotDisplay(display.craftingStation)
        )

        is ShapelessCraftingRecipeDisplay -> ShapelessCraftingRecipeDisplay(
            display.ingredients.map(::modifySlotDisplay),
            modifySlotDisplay(display.result),
            modifySlotDisplay(display.craftingStation)
        )

        is SmithingRecipeDisplay -> SmithingRecipeDisplay(
            modifySlotDisplay(display.template),
            modifySlotDisplay(display.base),
            modifySlotDisplay(display.addition),
            modifySlotDisplay(display.result),
            modifySlotDisplay(display.craftingStation)
        )

        is StonecutterRecipeDisplay -> StonecutterRecipeDisplay(
            modifySlotDisplay(display.input),
            modifySlotDisplay(display.result),
            modifySlotDisplay(display.craftingStation)
        )

        else -> {
            LOGGER.warn("Unknown recipe display type: ${display.javaClass}")
            display
        }
    }

    private fun modifySlotDisplay(display: SlotDisplay): SlotDisplay = when (display) {
        is SlotDisplay.Composite -> SlotDisplay.Composite(
            display.contents.map(::modifySlotDisplay)
        )

        is SlotDisplay.ItemStackSlotDisplay -> SlotDisplay.ItemStackSlotDisplay(
            display.stack.modify()
        )

        is SlotDisplay.SmithingTrimDemoSlotDisplay -> SlotDisplay.SmithingTrimDemoSlotDisplay(
            modifySlotDisplay(display.base),
            modifySlotDisplay(display.material),
            display.pattern
        )

        is SlotDisplay.WithRemainder -> SlotDisplay.WithRemainder(
            modifySlotDisplay(display.input),
            modifySlotDisplay(display.remainder)
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
     * 递归修改 Adventure Component
     *
     * @param component Adventure Component
     * @return 修改后的 Adventure Component
     */
    private fun modifyComponent(component: Component): Component {
        if (component !is TranslatableComponent)
            return component

        val modified = modifyComponent0(component)

        val modifiedChildren = modified.children().map { modifyComponent(it) }
        val modifiedArguments = modified.arguments().map { modifyComponent(it.asComponent()) }

        return modified.children(modifiedChildren).arguments(modifiedArguments)
    }

    private fun modifyComponent0(component: TranslatableComponent): TranslatableComponent {
        val hoverEvent = component.hoverEvent() ?: return component
        if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
            val itemStackInfo = hoverEvent.value() as HoverEvent.ShowItem
            val originItemStack = MojangStack(Registries.ITEM.getOrThrow(itemStackInfo.item()), itemStackInfo.count(), PaperAdventure.asVanilla(itemStackInfo.dataComponents()))
            val itemStack = originItemStack.modify()
            val newHover = itemStack.asBukkitMirror().asHoverEvent()
            return component.hoverEvent(newHover)
        }
        return component
    }

    private fun isCreative(event: PlayerPacketEvent<*>): Boolean {
        // 创造模式会1:1复制它接收到的物品到客户端本地,
        // 而我们发给客户端的萌芽物品并不是原始物品, 而是修改过的.
        // 问题在于, 修改过的萌芽物品并不包含任何 wakame 数据,
        // 这也就导致了创造模式会让物品栏内的萌芽物品失效.
        //
        // 这个问题的解决办法就是在物品上永远存一份原始数据的备份,
        // 但那样会导致额外的内存和性能开销. 不如等 Mojang 更新.
        //
        // 因此, 我们现阶段能做的就是忽略该问题.

        return event.player.gameMode == GameMode.CREATIVE
    }

    private fun MojangStack.modify(): MojangStack {
        if (!isNetworkRewrite)
            return this

        val bukkitCopy = asBukkitCopy()
        try {
            ItemRenderers.STANDARD.render(bukkitCopy)
        } catch (e: Throwable) {
            if (LOGGING) {
                LOGGER.error("An error occurred while rewrite network item: ${bukkitCopy.koishTypeId}", e)
            }
        }
        return bukkitCopy.toNMS()
    }
}

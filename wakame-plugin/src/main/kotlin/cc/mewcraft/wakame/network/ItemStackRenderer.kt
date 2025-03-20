package cc.mewcraft.wakame.network

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.network.event.PacketHandler
import cc.mewcraft.wakame.network.event.PacketListener
import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundContainerSetContentPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundContainerSetSlotPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundMerchantOffersPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundPlaceGhostRecipePacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundPlayerCombatKillPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundRecipeBookAddPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEntityDataPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEquipmentPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSystemChatPacketEvent
import cc.mewcraft.wakame.network.event.registerPacketListener
import cc.mewcraft.wakame.network.event.unregisterPacketListener
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.getOrThrow
import cc.mewcraft.wakame.util.item.editNbt
import cc.mewcraft.wakame.util.item.fastUpdate
import cc.mewcraft.wakame.util.item.isNetworkRewrite
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import com.mojang.datafixers.util.Pair
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.HoverEvent
import net.minecraft.core.component.DataComponentPredicate
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay
import net.minecraft.world.item.crafting.display.RecipeDisplay
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay
import net.minecraft.world.item.crafting.display.SlotDisplay
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "renderer")

/**
 * 修改 [net.minecraft.world.item.ItemStack].
 */
@Init(stage = InitStage.POST_WORLD)
internal object ItemStackRenderer : PacketListener, Listener {

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
            items[i] = getClientSideStack(item)
        }

        event.carriedItem = getClientSideStack(event.carriedItem)
    }

    @PacketHandler
    private fun handleSetSlot(event: ClientboundContainerSetSlotPacketEvent) {
        if (isCreative(event)) return
        event.item = getClientSideStack(event.item)
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
                    getClientSideStack(value)
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
                getClientSideStack(pair.second)
            )
        }
    }

    @PacketHandler
    private fun handleMerchantOffers(event: ClientboundMerchantOffersPacketEvent) {
        val newOffers = MerchantOffers()

        event.offers.forEach { offer ->
            val stackA = getClientSideStack(offer.baseCostA.itemStack)
            val costA = ItemCost(stackA.itemHolder, stackA.count, DataComponentPredicate.EMPTY, stackA)
            val costB = offer.costB.map {
                val stackB = getClientSideStack(it.itemStack)
                ItemCost(stackB.itemHolder, stackB.count, DataComponentPredicate.EMPTY, stackB)
            }
            newOffers += MerchantOffer(
                costA, costB, getClientSideStack(offer.result),
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
                    getClientSideRecipeDisplay(contents.display),
                    contents.group,
                    contents.category,
                    getClientSideIngredientList(contents.craftingRequirements)
                ),
                entry.notification(),
                entry.highlight()
            )
        }
    }

    @PacketHandler
    private fun handlePlaceGhostRecipe(event: ClientboundPlaceGhostRecipePacketEvent) {
        event.recipeDisplay = getClientSideRecipeDisplay(event.recipeDisplay)
    }

    @EventHandler
    private fun handlePlayerChat(event: AsyncChatEvent) {
        event.renderer { source, sourceDisplayName, message, viewer ->
            getClientSideTextComponent(message)
        }
    }

    @PacketHandler
    private fun handleSystemChat(event: ClientboundSystemChatPacketEvent) {
        event.message = getClientSideTextComponent(event.message)
    }

    @PacketHandler
    private fun handleCombatKill(event: ClientboundPlayerCombatKillPacketEvent) {
        event.message = getClientSideTextComponent(event.message)
    }

    private fun getClientSideIngredientList(optList: Optional<List<Ingredient>>): Optional<List<Ingredient>> =
        optList.map { ingredientList ->
            ingredientList.map { ingredient ->
                val itemStacks = ingredient.itemStacks()
                if (itemStacks != null)
                    Ingredient.ofStacks(itemStacks.map { getClientSideStack(it) })
                else ingredient
            }
        }

    private fun getClientSideRecipeDisplay(display: RecipeDisplay): RecipeDisplay = when (display) {
        is FurnaceRecipeDisplay -> FurnaceRecipeDisplay(
            getClientSideSlotDisplay(display.ingredient),
            getClientSideSlotDisplay(display.fuel),
            getClientSideSlotDisplay(display.result),
            getClientSideSlotDisplay(display.craftingStation),
            display.duration,
            display.experience
        )

        is ShapedCraftingRecipeDisplay -> ShapedCraftingRecipeDisplay(
            display.width, display.height,
            display.ingredients.map(::getClientSideSlotDisplay),
            getClientSideSlotDisplay(display.result),
            getClientSideSlotDisplay(display.craftingStation)
        )

        is ShapelessCraftingRecipeDisplay -> ShapelessCraftingRecipeDisplay(
            display.ingredients.map(::getClientSideSlotDisplay),
            getClientSideSlotDisplay(display.result),
            getClientSideSlotDisplay(display.craftingStation)
        )

        is SmithingRecipeDisplay -> SmithingRecipeDisplay(
            getClientSideSlotDisplay(display.template),
            getClientSideSlotDisplay(display.base),
            getClientSideSlotDisplay(display.addition),
            getClientSideSlotDisplay(display.result),
            getClientSideSlotDisplay(display.craftingStation)
        )

        is StonecutterRecipeDisplay -> StonecutterRecipeDisplay(
            getClientSideSlotDisplay(display.input),
            getClientSideSlotDisplay(display.result),
            getClientSideSlotDisplay(display.craftingStation)
        )

        else -> {
            LOGGER.warn("Unknown recipe display type: ${display.javaClass}")
            display
        }
    }

    private fun getClientSideSlotDisplay(display: SlotDisplay): SlotDisplay = when (display) {
        is SlotDisplay.Composite -> SlotDisplay.Composite(
            display.contents.map(::getClientSideSlotDisplay)
        )

        is SlotDisplay.ItemStackSlotDisplay -> SlotDisplay.ItemStackSlotDisplay(
            getClientSideStack(display.stack)
        )

        is SlotDisplay.SmithingTrimDemoSlotDisplay -> SlotDisplay.SmithingTrimDemoSlotDisplay(
            getClientSideSlotDisplay(display.base),
            getClientSideSlotDisplay(display.material),
            getClientSideSlotDisplay(display.pattern)
        )

        is SlotDisplay.WithRemainder -> SlotDisplay.WithRemainder(
            getClientSideSlotDisplay(display.input),
            getClientSideSlotDisplay(display.remainder)
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
    private fun getClientSideTextComponent(component: Component): Component {
        if (component !is TranslatableComponent)
            return component

        val modified = getClientSideTranslatableComponent(component)

        val modifiedChildren = modified.children().map { getClientSideTextComponent(it) }
        val modifiedArguments = modified.arguments().map { getClientSideTextComponent(it.asComponent()) }

        return modified.children(modifiedChildren).arguments(modifiedArguments)
    }

    private fun getClientSideTranslatableComponent(component: TranslatableComponent): TranslatableComponent {
        val hoverEvent = component.hoverEvent() ?: return component
        if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
            val itemStackInfo = hoverEvent.value() as HoverEvent.ShowItem
            val originItemStack = MojangStack(Registries.ITEM.getOrThrow(itemStackInfo.item()), itemStackInfo.count(), PaperAdventure.asVanilla(itemStackInfo.dataComponents()))
            val itemStack = getClientSideStack(originItemStack)
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

    private const val PDC_FIELD = "PublicBukkitValues"

    private fun getClientSideStack(itemStack: MojangStack): MojangStack {
        val newMojangStack = itemStack.copy()
        // 移除任意物品的 PDC
        newMojangStack.editNbt { nbt ->
            if (nbt.contains(PDC_FIELD)) {
                itemStack.processed = true
            }
            nbt.remove(PDC_FIELD)
        }

        val koishStack = newMojangStack.wrap()
        if (koishStack != null && itemStack.isNetworkRewrite) {
            try {
                ItemRenderers.STANDARD.render(koishStack)
            } catch (e: Throwable) {
                if (LOGGING) {
                    LOGGER.error("An error occurred while rewrite network item: ${koishStack.id}", e)
                }
            }
        }

        return newMojangStack
    }

    private const val PROCESSED_FIELD = "processed"

    private var MojangStack.processed: Boolean
        get() = get(DataComponents.CUSTOM_DATA)?.contains(PROCESSED_FIELD) == true
        set(value) {
            fastUpdate(
                type = DataComponents.CUSTOM_DATA,
                default = { CustomData.of(CompoundTag()) },
                applier = { customData ->
                    customData.update { nbt ->
                        if (value) {
                            nbt.put(PROCESSED_FIELD, ByteTag.ZERO)
                        } else {
                            nbt.remove(PROCESSED_FIELD)
                        }
                    }
                }
            )
        }
}

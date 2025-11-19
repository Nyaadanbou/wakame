package cc.mewcraft.wakame.item.display.implementation.crafting_station

import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.data.impl.ItemCrate
import cc.mewcraft.wakame.item.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item.datagen.impl.*
import cc.mewcraft.wakame.item.display.IndexedText
import cc.mewcraft.wakame.item.display.TextAssembler
import cc.mewcraft.wakame.item.display.implementation.*
import cc.mewcraft.wakame.item.display.implementation.common.*
import cc.mewcraft.wakame.item.display.implementation.standard.AttackSpeedRendererFormat
import cc.mewcraft.wakame.item.getData
import cc.mewcraft.wakame.item.getMeta
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.isNetworkRewrite
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.ExtraLore
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.item.fastLore
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DamageResistant
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.ItemEnchantments
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

internal class CraftingStationRendererFormatRegistry(renderer: CraftingStationItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class CraftingStationRendererLayout(renderer: CraftingStationItemRenderer) : AbstractRendererLayout(renderer)

/**
 * @param pos 物品出现的位置
 */
internal data class CraftingStationContext(
    val pos: Pos,
) {
    enum class Pos {
        CHOICE, // station choice
        RESULT, // station result
    }
}

@Init(
    stage = InitStage.POST_WORLD
)
@Reload
internal object CraftingStationItemRenderer : AbstractItemRenderer<CraftingStationContext>() {
    override val name: String = "crafting_station"
    override val formats = CraftingStationRendererFormatRegistry(this)
    override val layout = CraftingStationRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    @ReloadFun
    fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        CraftingStationRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: CraftingStationContext?) {
        requireNotNull(context) { "context" }

        // 这里的物品不应该被网络重写
        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        // ItemMetaTypes
        CraftingStationRenderingHandlerRegistry.CORE_CONTAINER.process(collector, item.getMeta(ItemMetaTypes.CORE_CONTAINER))
        CraftingStationRenderingHandlerRegistry.CUSTOM_NAME.process(collector, item.getMeta(ItemMetaTypes.CUSTOM_NAME))
        CraftingStationRenderingHandlerRegistry.ITEM_NAME.process(collector, item.getMeta(ItemMetaTypes.ITEM_NAME))
        CraftingStationRenderingHandlerRegistry.KIZAMI.process(collector, item.getMeta(ItemMetaTypes.KIZAMI))
        CraftingStationRenderingHandlerRegistry.ELEMENT.process(collector, item.getMeta(ItemMetaTypes.ELEMENT))

        // ItemPropTypes
        CraftingStationRenderingHandlerRegistry.ATTACK_SPEED.process(collector, item.getProp(ItemPropTypes.ATTACK_SPEED))
        CraftingStationRenderingHandlerRegistry.LORE.process(collector, item.getProp(ItemPropTypes.EXTRA_LORE))

        // ItemDataTypes
        CraftingStationRenderingHandlerRegistry.CRATE.process(collector, item.getData(ItemDataTypes.CRATE))
        CraftingStationRenderingHandlerRegistry.CORE.process(collector, item.getData(ItemDataTypes.CORE))

        // DataComponentTypes
        CraftingStationRenderingHandlerRegistry.DAMAGE_RESISTANT.process(collector, item.getData(DataComponentTypes.DAMAGE_RESISTANT))
        CraftingStationRenderingHandlerRegistry.ENCHANTMENTS.process(collector, item.getData(DataComponentTypes.ENCHANTMENTS))
        CraftingStationRenderingHandlerRegistry.FOOD.process(collector, item.getData(DataComponentTypes.FOOD))

        val koishLore = textAssembler.assemble(collector)

        // 将修改应用到物品上
        item.fastLore(koishLore)
    }
}

internal object CraftingStationRenderingHandlerRegistry : RenderingHandlerRegistry(CraftingStationItemRenderer) {
    @JvmField
    val ATTACK_SPEED: RenderingHandler<RegistryEntry<AttackSpeed>, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data)
    }

    @JvmField
    val CORE_CONTAINER: RenderingHandler<MetaCoreContainer, SingleValueRendererFormat> = configure("core_container") { data, format ->
        when (data) {
            is MetaCoreContainer.Static -> {
                format.render(Placeholder.component("count", Component.text(data.entry.size)))
            }

            is MetaCoreContainer.Dynamic -> {
                // TODO #373: 实现动态生成
                IndexedText.NOP
            }
        }
    }

    @JvmField
    val CRATE: RenderingHandler<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        format.render(
            // Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("level", Component.text(data.level)), // TODO display2 盲盒完成时再写这里
        )
    }

    @JvmField
    val ELEMENT: RenderingHandler<MetaElement, AggregateValueRendererFormat> = configure("element") { data, format ->
        when (data) {
            is MetaElement.Static -> {
                format.render(data.entries) { it.unwrap().displayName }
            }

            is MetaElement.Dynamic -> {
                val selector = data.entries
                val context = LootContext.default().apply { selectEverything = true }
                val allPossibleElements = selector.select(context)
                format.render(allPossibleElements, { it.unwrap().displayName })
            }
        }
    }

    @JvmField
    val CUSTOM_NAME: RenderingHandler<MetaCustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ENCHANTMENTS: RenderingHandler<ItemEnchantments, FuzzyEnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments())
    }

    @JvmField
    val DAMAGE_RESISTANT: RenderingHandler<DamageResistant, SingleValueRendererFormat> = configure("damage_resistant") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.types().key().asString())))
    }

    @JvmField
    val FOOD: RenderingHandler<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        format.render(
            Placeholder.component("nutrition", Component.text(data.nutrition())),
            Placeholder.component("saturation", Component.text(data.saturation())),
        )
    }

    @JvmField
    val ITEM_NAME: RenderingHandler<MetaItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    val KIZAMI: RenderingHandler<MetaKizami, AggregateValueRendererFormat> = configure("kizami") { data, format ->
        when (data) {
            is MetaKizami.Static -> {
                format.render(data.entries, { it.unwrap().displayName })
            }

            is MetaKizami.Dynamic -> {
                val selector = data.selector
                val allPossibleKizami = selector.select(LootContext.default()) // 进行一次随机的选择, 来向玩家展示可能出现的 Kizami
                format.render(allPossibleKizami, { it.unwrap().displayName })
            }
        }
    }

    @JvmField
    val LORE: RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingHandlers.LORE(this)

    @JvmField
    val CORE: RenderingHandler<Core, FuzzyCoreRendererFormat> = configure("core") { data, format ->
        format.render(data)
    }
}

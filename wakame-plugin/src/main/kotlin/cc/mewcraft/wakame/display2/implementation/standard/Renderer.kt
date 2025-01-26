package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.*
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ExtraLore
import cc.mewcraft.wakame.item.templates.components.ItemArrow
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.nio.file.Path


internal class StandardRendererFormatRegistry(renderer: StandardItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class StandardRendererLayout(renderer: StandardItemRenderer) : AbstractRendererLayout(renderer)

internal data object StandardContext // 等之后需要的时候, 改成 class 即可

@Init(
    stage = InitStage.POST_WORLD
)
@Reload
internal object StandardItemRenderer : AbstractItemRenderer<PacketNekoStack, StandardContext>() {
    override val name = "standard"
    override val formats = StandardRendererFormatRegistry(this)
    override val layout = StandardRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    @ReloadFun
    fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(
        formatPath: Path,
        layoutPath: Path,
    ) {
        StandardRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath) // formats 必须在 layout 之前初始化
        layout.initialize(layoutPath)
    }

    override fun render(item: PacketNekoStack, context: StandardContext?) {
        requireNotNull(context) { "context" }

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.ARROW) { data -> StandardRenderingHandlerRegistry.ARROW.process(collector, data) }

        // 对于最可能被频繁修改的 `custom_name`, `item_name`, `lore` 直接读取配置模板里的内容
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> StandardRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> StandardRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.LORE) { data -> StandardRenderingHandlerRegistry.LORE.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ATTACK_SPEED) { data -> StandardRenderingHandlerRegistry.ATTACK_SPEED.process(collector, data) }
        components.process(ItemComponentTypes.CELLS) { data -> for ((_, cell) in data) renderCore(collector, cell.getCore()) }
        components.process(ItemComponentTypes.CRATE) { data -> StandardRenderingHandlerRegistry.CRATE.process(collector, data) }
        components.process(ItemComponentTypes.ELEMENTS) { data -> StandardRenderingHandlerRegistry.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> StandardRenderingHandlerRegistry.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.DAMAGE_RESISTANT) { data -> StandardRenderingHandlerRegistry.DAMAGE_RESISTANT.process(collector, Unit) }
        components.process(ItemComponentTypes.FOOD) { data -> StandardRenderingHandlerRegistry.FOOD.process(collector, data) }
        components.process(ItemComponentTypes.KIZAMIZ) { data -> StandardRenderingHandlerRegistry.KIZAMIZ.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> StandardRenderingHandlerRegistry.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> StandardRenderingHandlerRegistry.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1: ItemRarity = data1 ?: return@process
            val data2: ReforgeHistory = data2 ?: ReforgeHistory.ZERO
            StandardRenderingHandlerRegistry.RARITY.process(collector, data1, data2)
        }
        components.process(ItemComponentTypes.STORED_ENCHANTMENTS) { data -> StandardRenderingHandlerRegistry.ENCHANTMENTS.process(collector, data) }

        val itemLore = textAssembler.assemble(collector)

        item.erase()

        item.lore = run {
            // 尝试在物品原本的 lore 的第一行插入我们渲染的 lore.
            // 如果原本的 lore 为空, 则直接使用我们渲染的 lore.
            // 如果原本的 lore 不为空, 则在渲染的 lore 和原本的 lore 之间插入一个空行.

            val lore = item.lore
            if (lore.isNullOrEmpty()) {
                itemLore
            } else {
                itemLore + buildList {
                    add(Component.empty())
                    addAll(lore)
                }
            }
        }
        item.showAttributeModifiers(false)
        item.showEnchantments(false)
        item.showStoredEnchantments(false)
    }

    private fun renderCore(collector: ReferenceOpenHashSet<IndexedText>, core: Core) {
        when (core) {
            is AttributeCore -> StandardRenderingHandlerRegistry.CELLULAR_ATTRIBUTE.process(collector, core)
            is AbilityCore -> StandardRenderingHandlerRegistry.CELLULAR_ABILITY.process(collector, core)
            is EmptyCore -> StandardRenderingHandlerRegistry.CELLULAR_EMPTY.process(collector, core)
        }
    }
}

internal object StandardRenderingHandlerRegistry : RenderingHandlerRegistry(StandardItemRenderer) {
    @JvmField
    val ARROW: RenderingHandler<ItemArrow, ListValueRendererFormat> = configure("arrow") { data, format ->
        format.render(
            Placeholder.component("pierce_level", Component.text(data.pierceLevel)),
            Placeholder.component("fire_ticks", Component.text(data.fireTicks)),
            Placeholder.component("hit_fire_ticks", Component.text(data.hitFireTicks)),
            Placeholder.component("hit_frozen_ticks", Component.text(data.hitFrozenTicks)),
            Placeholder.component("glow_ticks", Component.text(data.glowTicks)),
        )
    }

    @JvmField
    val ATTACK_SPEED: RenderingHandler<ItemAttackSpeed, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data.level)
    }

    @JvmField
    val CELLULAR_ATTRIBUTE: RenderingHandler<AttributeCore, CellularAttributeRendererFormat> = configure("cells/attributes") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_ABILITY: RenderingHandler<AbilityCore, CellularAbilityRendererFormat> = configure("cells/abilities") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_EMPTY: RenderingHandler<EmptyCore, CellularEmptyRendererFormat> = configure("cells/empty") { data, format ->
        format.render(data)
    }

    @JvmField
    val CRATE: RenderingHandler<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        format.render(
            Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("name", Component.text(data.identity)), // TODO display2 盲盒完成时再写这里)
        )
    }

    @JvmField
    val CUSTOM_NAME: RenderingHandler<CustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingHandler<ItemElements, AggregateValueRendererFormat> = CommonRenderingHandlers.ELEMENTS(this)

    @JvmField
    val ENCHANTMENTS: RenderingHandler<ItemEnchantments, EnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments)
    }

    @JvmField
    val DAMAGE_RESISTANT: RenderingHandler<Unit, SingleValueRendererFormat> = configure("damage_resistant") { _, format ->
        format.render()
    }

    @JvmField
    val FOOD: RenderingHandler<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        format.render(
            Placeholder.component("nutrition", Component.text(data.nutrition)),
            Placeholder.component("saturation", Component.text(data.saturation)),
        )
    }

    @JvmField
    val ITEM_NAME: RenderingHandler<ItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val KIZAMIZ: RenderingHandler<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        format.render(data.kizamiz) { it.value.displayName }
    }

    @JvmField
    val LEVEL: RenderingHandler<ItemLevel, SingleValueRendererFormat> = CommonRenderingHandlers.LEVEL(this)

    @JvmField
    val LORE: RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingHandlers.LORE(this)

    @JvmField
    val PORTABLE_CORE: RenderingHandler<PortableCore, PortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: RenderingHandler2<ItemRarity, ReforgeHistory, RarityRendererFormat> = CommonRenderingHandlers.RARITY(this)
}

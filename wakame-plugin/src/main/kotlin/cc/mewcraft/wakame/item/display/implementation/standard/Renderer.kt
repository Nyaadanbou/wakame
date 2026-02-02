package cc.mewcraft.wakame.item.display.implementation.standard

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.*
import cc.mewcraft.wakame.item.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item.display.IndexedText
import cc.mewcraft.wakame.item.display.ItemRendererConstants
import cc.mewcraft.wakame.item.display.NetworkRenderer
import cc.mewcraft.wakame.item.display.TextAssembler
import cc.mewcraft.wakame.item.display.implementation.*
import cc.mewcraft.wakame.item.display.implementation.common.*
import cc.mewcraft.wakame.item.extension.*
import cc.mewcraft.wakame.item.feature.EnchantSlotFeature
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.configurate.yamlLoader
import cc.mewcraft.wakame.util.item.fastLore
import cc.mewcraft.wakame.util.item.fastLoreOrEmpty
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.datacomponent.item.TooltipDisplay
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Registry
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.kotlin.extensions.getList
import xyz.xenondevs.commons.collections.takeUnlessEmpty
import java.nio.file.Path
import kotlin.io.path.readText

internal class StandardRendererFormatRegistry(renderer: StandardItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class StandardRendererLayout(renderer: StandardItemRenderer) : AbstractRendererLayout(renderer)

@Init(InitStage.POST_WORLD)
internal object StandardItemRenderer : AbstractItemRenderer<Nothing>() {
    override val name = "standard"
    override val formats = StandardRendererFormatRegistry(this)
    override val layout = StandardRendererLayout(this)
    private val textAssembler = TextAssembler(layout)
    private val removeComponents: MutableList<DataComponentType> = ObjectArrayList()
    private val hiddenComponents: MutableSet<DataComponentType> = ObjectArraySet()

    @InitFun
    fun init() {
        loadDataFromConfigs()
        loadExtraDataFromConfigs()
    }

    fun reload() {
        loadDataFromConfigs()
        loadExtraDataFromConfigs()
    }

    fun loadExtraDataFromConfigs() {
        // 加载 remove_components
        val renderersDirectory = KoishDataPaths.CONFIGS.resolve(ItemRendererConstants.DATA_DIR)
        val layoutPath = renderersDirectory.resolve(name).resolve(ItemRendererConstants.LAYOUT_FILE_NAME)
        val yaml = yamlLoader { withDefaults() }.buildAndLoadString(layoutPath.readText())
        removeComponents.clear()
        removeComponents += yaml.node("remove_components").getList<DataComponentType>(emptyList())
        hiddenComponents.clear()
        hiddenComponents += yaml.node("hidden_components").getList<DataComponentType>(emptyList())

    }

    override fun initialize(
        formatPath: Path,
        layoutPath: Path,
    ) {
        StandardRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath) // formats 必须在 layout 之前初始化
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: Nothing?) {
        if (!(NetworkRenderer.responsible(item))) {
            return
        }

        val collector = ReferenceOpenHashSet<IndexedText>()

        StandardRenderingHandlerRegistry.ARROW.process(collector, item.getProp(ItemPropTypes.ARROW))
        StandardRenderingHandlerRegistry.ATTACK_SPEED.process(collector, item.getProp(ItemPropTypes.ATTACK_SPEED))
        StandardRenderingHandlerRegistry.LORE.process(collector, item.getProp(ItemPropTypes.EXTRA_LORE))
        StandardRenderingHandlerRegistry.ENTITY_BUCKET_PROP.process(collector, item.getProp(ItemPropTypes.ENTITY_BUCKET))
        StandardRenderingHandlerRegistry.FUEL.process(collector, item.getProp(ItemPropTypes.FUEL))
        StandardRenderingHandlerRegistry.CASTABLE.process(collector, item.getProp(ItemPropTypes.CASTABLE))
        StandardRenderingHandlerRegistry.ITEM_NAME.process(collector, item.getMeta(ItemMetaTypes.ITEM_NAME)) // 对于最可能被频繁修改的 `item_name`, `custom_name`
        StandardRenderingHandlerRegistry.CUSTOM_NAME.process(collector, item.getMeta(ItemMetaTypes.CUSTOM_NAME))
        StandardRenderingHandlerRegistry.CRATE.process(collector, item.getData(ItemDataTypes.CRATE))
        StandardRenderingHandlerRegistry.ELEMENT.process(collector, item.elements.takeUnlessEmpty())
        StandardRenderingHandlerRegistry.KIZAMI.process(collector, item.kizamiz.takeUnlessEmpty())
        StandardRenderingHandlerRegistry.LEVEL.process(collector, item.level)
        StandardRenderingHandlerRegistry.CORE.process(collector, item.core)
        renderCoreContainer(collector, item.coreContainer)
        StandardRenderingHandlerRegistry.RARITY.process(collector, item.rarity2, item.getData(ItemDataTypes.REFORGE_HISTORY) ?: ReforgeHistory.ZERO)
        StandardRenderingHandlerRegistry.ENTITY_BUCKET_INFO.process(collector, item.getData(ItemDataTypes.ENTITY_BUCKET_INFO))
        StandardRenderingHandlerRegistry.ENCHANTMENTS.process(collector, item.getData(DataComponentTypes.ENCHANTMENTS))
        StandardRenderingHandlerRegistry.DAMAGE_RESISTANT.process(collector, if (item.hasData(DataComponentTypes.DAMAGE_RESISTANT)) Unit else null)
        StandardRenderingHandlerRegistry.FOOD.process(collector, item.getData(DataComponentTypes.FOOD))
        StandardRenderingHandlerRegistry.ENCHANT_SLOTS.process(collector, item)

        // 生成 koish lore
        val koishLore = textAssembler.assemble(collector)

        // 尝试在物品原本的 lore 的最后一行插入我们渲染的 lore:
        // - 如果原本的 lore 为空, 则直接使用我们渲染的 lore
        // - 如果原本的 lore 不为空, 则在 koish lore 和 existing lore 之间插入一个空行
        val existingLore = item.fastLoreOrEmpty
        val resultantLore = if (existingLore.isEmpty() && koishLore.isNotEmpty()) {
            // existingLore 为空 && koishLore 不为空
            koishLore
        } else if (existingLore.isEmpty()) {
            // existingLore 为空 && koishLore 为空
            return
        } else if (koishLore.isEmpty()) {
            // existingLore 不为空 && koishLore 为空
            existingLore
        } else {
            // existingLore 不为空 && koishLore 不为空
            buildList {
                addAll(existingLore)
                add(Component.empty()) // 加入空行分隔开
                addAll(koishLore)
            }
        }

        // 应用 lore 到物品上
        item.fastLore(resultantLore)

        // 移除不需要的物品组件
        for (componentType in removeComponents) {
            item.unsetData(componentType)
        }

        // 隐藏不需要显示的物品组件
        if (hiddenComponents.isNotEmpty()) {
            item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hiddenComponents(hiddenComponents))
        }

        // 热修复攻击冷却不显示问题
        HotfixWeaponCooldownDisplay.transform(item)
    }

    private fun renderCoreContainer(collector: ReferenceOpenHashSet<IndexedText>, coreContainer: CoreContainer?) {
        if (coreContainer == null) return
        for ((_, core) in coreContainer) {
            when (core) {
                is AttributeCore -> StandardRenderingHandlerRegistry.CORE_ATTRIBUTE.process(collector, core)
                is EmptyCore -> StandardRenderingHandlerRegistry.CORE_EMPTY.process(collector, core)
                is VirtualCore -> IndexedText.NOP
            }
        }
    }
}

internal object StandardRenderingHandlerRegistry : RenderingHandlerRegistry(StandardItemRenderer) {

    @JvmField
    val ARROW: RenderingHandler<Arrow, ListValueRendererFormat> = configure("arrow") { data, format ->
        format.render(
            Placeholder.component("pierce_level", Component.text(data.pierceLevel)),
            Placeholder.component("fire_ticks", Component.text(data.fireTicks)),
            Placeholder.component("hit_fire_ticks", Component.text(data.hitFireTicks)),
            Placeholder.component("hit_frozen_ticks", Component.text(data.hitFrozenTicks)),
            Placeholder.component("glow_ticks", Component.text(data.glowTicks)),
        )
    }

    @JvmField
    val ATTACK_SPEED: RenderingHandler<RegistryEntry<AttackSpeed>, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data)
    }

    @JvmField
    val CORE_ATTRIBUTE: RenderingHandler<AttributeCore, CoreAttributeRendererFormat> = configure("core/attributes") { data, format ->
        format.render(data)
    }

    @JvmField
    val CORE_EMPTY: RenderingHandler<EmptyCore, CoreEmptyRendererFormat> = configure("core/empty") { data, format ->
        format.render(data)
    }

    @JvmField
    val CRATE: RenderingHandler<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        format.render(
            Placeholder.component("level", Component.text(data.level)) // TODO display2 盲盒完成时再写这里)
        )
    }

    @JvmField
    val CUSTOM_NAME: RenderingHandler<MetaCustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENT: RenderingHandler<Set<RegistryEntry<Element>>, AggregateValueRendererFormat> = CommonRenderingHandlers.ELEMENT(this)

    @JvmField
    val ENCHANTMENTS: RenderingHandler<ItemEnchantments, EnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments())
    }

    @JvmField
    val ENCHANT_SLOTS: RenderingHandler<ItemStack, SingleValueRendererFormat> = configure("enchant_slots") { data, format ->
        val used = EnchantSlotFeature.getSlotUsed(data)
        val maxBase = EnchantSlotFeature.getSlotBase(data)
        val maxExtra = EnchantSlotFeature.getSlotExtra(data)
        val maxTotal = maxBase + maxExtra
        if (maxTotal <= 0) return@configure IndexedText.NOP
        val resolver = TagResolver.resolver(
            Placeholder.component("used", Component.text(used)),
            Placeholder.component("max_base", Component.text(maxBase)),
            Placeholder.component("max_extra", Component.text(maxExtra)),
            Placeholder.component("max_total", Component.text(maxTotal)),
        )
        format.render(resolver)
    }

    @JvmField
    val DAMAGE_RESISTANT: RenderingHandler<Unit, SingleValueRendererFormat> = configure("damage_resistant") { _, format ->
        format.render()
    }

    @JvmField
    val FOOD: RenderingHandler<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        format.render(
            Placeholder.component("nutrition", Component.text(data.nutrition())),
            Placeholder.component("saturation", Component.text(data.saturation()))
        )
    }

    @JvmField
    val ITEM_NAME: RenderingHandler<MetaItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val KIZAMI: RenderingHandler<Set<RegistryEntry<Kizami>>, AggregateValueRendererFormat> = configure("kizami") { data, format ->
        format.render(data) { it.unwrap().displayName }
    }

    @JvmField
    val LEVEL: RenderingHandler<ItemLevel, SingleValueRendererFormat> = CommonRenderingHandlers.LEVEL(this)

    @JvmField
    val LORE: RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingHandlers.LORE(this)

    @JvmField
    val CORE: RenderingHandler<Core, CoreRendererFormat> = configure("core") { data, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: RenderingHandler2<RegistryEntry<Rarity>, ReforgeHistory, RarityRendererFormat> = CommonRenderingHandlers.RARITY(this)

    @JvmField
    val ENTITY_BUCKET_PROP: RenderingHandler<EntityBucket, AggregateValueRendererFormat> = configure("entity_bucket_prop") { data, format ->
        format.render(data.allowedEntityTypes) { Component.translatable(Registry.ENTITY_TYPE.getOrThrow(it)) }
    }

    @JvmField
    val ENTITY_BUCKET_INFO: RenderingHandler<EntityBucketInfo, EntityBucketInfoRendererFormat> = configure("entity_bucket_info") { data, format ->
        format.render(data)
    }

    @JvmField
    val FUEL: RenderingHandler<Fuel, ListValueRendererFormat> = configure("fuel") { data, format ->
        format.render(
            Placeholder.component("burn_time", Component.text(data.burnTime)),
            Placeholder.component("consume", Component.text(data.consume))
        )
    }

    @JvmField
    val CASTABLE: RenderingHandler<Map<String, Castable>, CastableRendererFormat> = configure("castable") { data, format -> format.render(data) }
}

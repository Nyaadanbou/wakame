package cc.mewcraft.wakame.item.display.implementation.standard

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.*
import cc.mewcraft.wakame.item.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item.display.IndexedText
import cc.mewcraft.wakame.item.display.TextAssembler
import cc.mewcraft.wakame.item.display.implementation.*
import cc.mewcraft.wakame.item.display.implementation.common.*
import cc.mewcraft.wakame.item.extension.*
import cc.mewcraft.wakame.item.feature.EnchantSlotFeature
import cc.mewcraft.wakame.item.getData
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.Arrow
import cc.mewcraft.wakame.item.property.impl.EntityBucket
import cc.mewcraft.wakame.item.property.impl.ExtraLore
import cc.mewcraft.wakame.item.property.impl.Fuel
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.item.fastLore
import cc.mewcraft.wakame.util.item.fastLoreOrEmpty
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.ItemEnchantments
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Registry
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.collections.takeUnlessEmpty
import java.nio.file.Path

internal class StandardRendererFormatRegistry(renderer: StandardItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class StandardRendererLayout(renderer: StandardItemRenderer) : AbstractRendererLayout(renderer)

@Init(stage = InitStage.POST_WORLD)
@Reload
internal object StandardItemRenderer : AbstractItemRenderer<Nothing>() {
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

    override fun render(item: ItemStack, context: Nothing?) {
        val collector = ReferenceOpenHashSet<IndexedText>()

        item.process(ItemPropTypes.ARROW) { data -> StandardRenderingHandlerRegistry.ARROW.process(collector, data) }
        item.process(ItemPropTypes.ATTACK_SPEED) { data -> StandardRenderingHandlerRegistry.ATTACK_SPEED.process(collector, data) }
        item.process(ItemPropTypes.EXTRA_LORE) { data -> StandardRenderingHandlerRegistry.LORE.process(collector, data) }
        item.process(ItemPropTypes.ENTITY_BUCKET) { data -> StandardRenderingHandlerRegistry.ENTITY_BUCKET_PROP.process(collector, data) }
        item.process(ItemPropTypes.FUEL) { data -> StandardRenderingHandlerRegistry.FUEL.process(collector, data) }

        // 对于最可能被频繁修改的 `item_name`, `custom_name` 直接读取配置模板里的内容
        item.process(ItemMetaTypes.ITEM_NAME) { data -> StandardRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }
        item.process(ItemMetaTypes.CUSTOM_NAME) { data -> StandardRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }

        item.process(ItemDataTypes.CRATE) { data -> StandardRenderingHandlerRegistry.CRATE.process(collector, data) }

        item.elements.takeUnlessEmpty()?.let { StandardRenderingHandlerRegistry.ELEMENT.process(collector, it) }
        item.kizamiz.takeUnlessEmpty()?.let { StandardRenderingHandlerRegistry.KIZAMI.process(collector, it) }
        item.level?.let { StandardRenderingHandlerRegistry.LEVEL.process(collector, it) }
        item.core?.let { StandardRenderingHandlerRegistry.CORE.process(collector, it) }
        item.coreContainer?.let { data -> for ((_, core) in data) renderCore(collector, core) }
        item.rarity2?.let { rarityEntry ->
            val data2 = item.getData(ItemDataTypes.REFORGE_HISTORY) ?: ReforgeHistory.ZERO
            StandardRenderingHandlerRegistry.RARITY.process(collector, rarityEntry, data2)
        }

        item.process(ItemDataTypes.ENTITY_BUCKET_INFO) { data -> StandardRenderingHandlerRegistry.ENTITY_BUCKET_INFO.process(collector, data) }

        item.process(DataComponentTypes.ENCHANTMENTS) { data -> StandardRenderingHandlerRegistry.ENCHANTMENTS.process(collector, data) }
        item.process(DataComponentTypes.DAMAGE_RESISTANT) { data -> StandardRenderingHandlerRegistry.DAMAGE_RESISTANT.process(collector, Unit) }
        item.process(DataComponentTypes.FOOD) { data -> StandardRenderingHandlerRegistry.FOOD.process(collector, data) }

        StandardRenderingHandlerRegistry.ENCHANT_SLOTS.process(collector, item)

        val lore = textAssembler.assemble(collector)
        item.fastLore(run {
            // 尝试在物品原本的 lore 的第一行插入我们渲染的 lore.
            // 如果原本的 lore 为空, 则直接使用我们渲染的 lore.
            // 如果原本的 lore 不为空, 则在渲染的 lore 和原本的 lore 之间插入一个空行.

            val existingLore = item.fastLoreOrEmpty
            if (existingLore.isEmpty()) {
                lore
            } else {
                buildList {
                    addAll(existingLore)
                    add(Component.empty())
                    addAll(lore)
                }
            }
        })
    }

    private fun renderCore(collector: ReferenceOpenHashSet<IndexedText>, core: Core) {
        when (core) {
            is AttributeCore -> StandardRenderingHandlerRegistry.CORE_ATTRIBUTE.process(collector, core)
            is EmptyCore -> StandardRenderingHandlerRegistry.CORE_EMPTY.process(collector, core)
            is VirtualCore -> IndexedText.NOP
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
}

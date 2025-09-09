package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.config.property.impl.Arrow
import cc.mewcraft.wakame.item2.config.property.impl.EntityBucket
import cc.mewcraft.wakame.item2.config.property.impl.ExtraLore
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.*
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.item.*
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.ItemEnchantments
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Registry
import org.bukkit.inventory.ItemStack
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

        item.process(ItemPropertyTypes.ARROW) { data -> StandardRenderingHandlerRegistry.ARROW.process(collector, data) }
        item.process(ItemPropertyTypes.ATTACK_SPEED) { data -> StandardRenderingHandlerRegistry.ATTACK_SPEED.process(collector, data) }
        item.process(ItemPropertyTypes.EXTRA_LORE) { data -> StandardRenderingHandlerRegistry.LORE.process(collector, data) }
        item.process(ItemPropertyTypes.ENTITY_BUCKET) { data -> StandardRenderingHandlerRegistry.ENTITY_BUCKET_PROP.process(collector, data) }

        // 对于最可能被频繁修改的 `item_name`, `custom_name` 直接读取配置模板里的内容
        item.process(ItemMetaTypes.ITEM_NAME) { data -> StandardRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }
        item.process(ItemMetaTypes.CUSTOM_NAME) { data -> StandardRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }

        item.process(ItemDataTypes.CORE_CONTAINER) { data -> for ((_, core) in data) renderCore(collector, core) }
        item.process(ItemDataTypes.CRATE) { data -> StandardRenderingHandlerRegistry.CRATE.process(collector, data) }
        item.process(ItemDataTypes.ELEMENT) { data -> StandardRenderingHandlerRegistry.ELEMENT.process(collector, data) }
        item.process(ItemDataTypes.KIZAMI) { data -> StandardRenderingHandlerRegistry.KIZAMI.process(collector, data) }
        item.process(ItemDataTypes.LEVEL) { data -> StandardRenderingHandlerRegistry.LEVEL.process(collector, data) }
        item.process(ItemDataTypes.CORE) { data -> StandardRenderingHandlerRegistry.CORE.process(collector, data) }
        item.process(ItemDataTypes.RARITY, ItemDataTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1 = data1 ?: return@process
            val data2 = data2 ?: ReforgeHistory.ZERO
            StandardRenderingHandlerRegistry.RARITY.process(collector, data1, data2)
        }
        item.process(ItemDataTypes.ENTITY_BUCKET_INFO) { data -> StandardRenderingHandlerRegistry.ENTITY_BUCKET_INFO.process(collector, data) }

        item.process(DataComponentTypes.ENCHANTMENTS) { data -> StandardRenderingHandlerRegistry.ENCHANTMENTS.process(collector, data) }
        item.process(DataComponentTypes.DAMAGE_RESISTANT) { data -> StandardRenderingHandlerRegistry.DAMAGE_RESISTANT.process(collector, Unit) }
        item.process(DataComponentTypes.FOOD) { data -> StandardRenderingHandlerRegistry.FOOD.process(collector, data) }

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

        item.hideAttributeModifiers()
        item.hideEnchantments()
        item.hideStoredEnchantments()
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
        format.render(data.allowedEntities) { Component.translatable(Registry.ENTITY_TYPE.getOrThrow(it)) }
    }

    @JvmField
    val ENTITY_BUCKET_INFO: RenderingHandler<EntityBucketInfo, EntityBucketInfoRendererFormat> = configure("entity_bucket_info") { data, format ->
        format.render(data)
    }
}

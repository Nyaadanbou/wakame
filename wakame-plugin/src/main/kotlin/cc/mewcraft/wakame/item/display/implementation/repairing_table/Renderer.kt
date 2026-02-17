package cc.mewcraft.wakame.item.display.implementation.repairing_table

import cc.mewcraft.wakame.item.display.IndexedText
import cc.mewcraft.wakame.item.display.TextAssembler
import cc.mewcraft.wakame.item.display.implementation.*
import cc.mewcraft.wakame.item.display.implementation.common.ListValueRendererFormat
import cc.mewcraft.wakame.item.isNetworkRewrite
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.fastLore
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import java.nio.file.Path


internal class RepairingTableRendererFormatRegistry : AbstractRendererFormatRegistry(RepairingTableItemRenderer)

internal class RepairingTableItemRendererLayout : AbstractRendererLayout(RepairingTableItemRenderer)

internal data class RepairingTableItemRendererContext(val damage: Int, val maxDamage: Int, val repairCost: Double)

@Init(InitStage.POST_WORLD)
internal object RepairingTableItemRenderer : AbstractItemRenderer<ItemStack, RepairingTableItemRendererContext>() {
    override val name: String = "repairing_table"
    override val formats: AbstractRendererFormatRegistry = RepairingTableRendererFormatRegistry()
    override val layout: AbstractRendererLayout = RepairingTableItemRendererLayout()
    private val textAssembler: TextAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RepairingTableRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: RepairingTableItemRendererContext?) {
        requireNotNull(context) { "context" }

        // 这里的物品不应该被网络重写
        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        // Context
        RepairingTableRenderingHandlerRegistry.DURABILITY.process(collector, context)
        RepairingTableRenderingHandlerRegistry.REPAIR_COST.process(collector, context)
        RepairingTableRenderingHandlerRegistry.REPAIR_USAGE.process(collector, context)

        val koishLore = textAssembler.assemble(collector)

        // 应用修改到物品上
        item.fastLore(koishLore)
    }
}

internal object RepairingTableRenderingHandlerRegistry : RenderingHandlerRegistry(RepairingTableItemRenderer) {
    @JvmField
    val DURABILITY: RenderingHandler<RepairingTableItemRendererContext, ListValueRendererFormat> = configure("durability") { context, format ->
        val damage = text(context.damage)
        val maxDamage = text(context.maxDamage)
        val durability = text(context.maxDamage - context.damage)
        format.render(
            Placeholder.component("damage", damage),
            Placeholder.component("max_damage", maxDamage),
            Placeholder.component("durability", durability)
        )
    }

    @JvmField
    val REPAIR_COST: RenderingHandler<RepairingTableItemRendererContext, ListValueRendererFormat> = configure("repair_cost") { context, format ->
        val value = context.repairCost + 1 // 显示上永远 +1 让边界情况看起来更合理
        format.render(
            Formatter.number("value", value)
        )
    }

    @JvmField
    val REPAIR_USAGE: RenderingHandler<RepairingTableItemRendererContext, ListValueRendererFormat> = configure("repair_usage") { _, format ->
        format.render()
    }
}

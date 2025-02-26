package cc.mewcraft.wakame.display2.implementation.repairing_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.ListValueRendererFormat
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.item.fastLore
import cc.mewcraft.wakame.util.item.hideAll
import cc.mewcraft.wakame.util.item.isClientSide
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import java.nio.file.Path


internal class RepairingTableRendererFormatRegistry : AbstractRendererFormatRegistry(RepairingTableItemRenderer)

internal class RepairingTableItemRendererLayout : AbstractRendererLayout(RepairingTableItemRenderer)

internal data class RepairingTableItemRendererContext(val damage: Int, val maxDamage: Int, val repairCost: Double)

@Init(
    stage = InitStage.POST_WORLD
)
@Reload
internal object RepairingTableItemRenderer : AbstractItemRenderer<ItemStack, RepairingTableItemRendererContext>() {
    override val name: String = "repairing_table"
    override val formats: AbstractRendererFormatRegistry = RepairingTableRendererFormatRegistry()
    override val layout: AbstractRendererLayout = RepairingTableItemRendererLayout()
    private val textAssembler: TextAssembler = TextAssembler(layout)

    @InitFun
    private fun init() {
        loadDataFromConfigs()
    }

    @ReloadFun
    private fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RepairingTableRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: RepairingTableItemRendererContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()
        RepairingTableRenderingHandlerRegistry.DURABILITY.process(collector, context)
        RepairingTableRenderingHandlerRegistry.REPAIR_COST.process(collector, context)
        RepairingTableRenderingHandlerRegistry.REPAIR_USAGE.process(collector, context)

        val lore = textAssembler.assemble(collector)
        item.fastLore(lore)

        item.hideAll()
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

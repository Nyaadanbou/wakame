package cc.mewcraft.wakame.display2.implementation.repairing_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.util.*
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

internal class RepairingTableItemRendererFormats : AbstractRendererFormats(RepairingTableItemRenderer)
internal class RepairingTableItemRendererLayout : AbstractRendererLayout(RepairingTableItemRenderer)

internal data class RepairingTableItemRendererContext(
    val damage: Int,
    val maxDamage: Int,
    val repairCost: Double,
)

internal object RepairingTableItemRenderer : AbstractItemRenderer<ItemStack, RepairingTableItemRendererContext>() {
    override val name: String = "repairing_table"
    override val formats: AbstractRendererFormats = RepairingTableItemRendererFormats()
    override val layout: AbstractRendererLayout = RepairingTableItemRendererLayout()
    private val textAssembler: TextAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RepairingTableItemRendererParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: RepairingTableItemRendererContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        // 渲染 `minecraft:item_name`
        // 不做处理

        // 渲染 `minecraft:lore`
        val collector = ReferenceOpenHashSet<IndexedText>()
        RepairingTableItemRendererParts.DURABILITY.process(collector, context)
        RepairingTableItemRendererParts.REPAIR_COST.process(collector, context)
        RepairingTableItemRendererParts.REPAIR_USAGE.process(collector, context)
        item.lore0 = textAssembler.assemble(collector)

        // 渲染 `minecraft:custom_model_data`
        val nekoStack = item.shadowNeko()
        if (nekoStack != null) {
            item.customModelData = ItemModelDataLookup[nekoStack.id, nekoStack.variant]
        }

        // 渲染其他可见部分
        item.showNothing()
    }
}

internal object RepairingTableItemRendererParts : RenderingParts(RepairingTableItemRenderer) {
    @JvmField
    val DURABILITY: RenderingPart<RepairingTableItemRendererContext, ListValueRendererFormat> = configure("durability") { context, format ->
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
    val REPAIR_COST: RenderingPart<RepairingTableItemRendererContext, ListValueRendererFormat> = configure("repair_cost") { context, format ->
        val value = context.repairCost + 1 // 显示上永远 +1 让边界情况看起来更合理
        format.render(
            Formatter.number("value", value)
        )
    }

    @JvmField
    val REPAIR_USAGE: RenderingPart<RepairingTableItemRendererContext, ListValueRendererFormat> = configure("repair_usage") { _, format ->
        format.render()
    }
}

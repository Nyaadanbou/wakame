package cc.mewcraft.wakame.display2.implementation.repairing_table

import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.item.templates.components.*
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

internal class RepairingTableItemRendererFormats : AbstractRendererFormats(RepairingTableItemRenderer)
internal class RepairingTableItemRendererLayout : AbstractRendererLayout(RepairingTableItemRenderer)

internal class RepairingTableItemRendererContext

internal object RepairingTableItemRenderer : AbstractItemRenderer<ItemStack, RepairingTableItemRendererContext>() {
    override val name: String = "repairing_table"
    override val formats: AbstractRendererFormats = RepairingTableItemRendererFormats()
    override val layout: AbstractRendererLayout = RepairingTableItemRendererLayout()

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RepairingTableItemRendererParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: RepairingTableItemRendererContext?) {
        requireNotNull(context) { "context" }
    }
}

internal object RepairingTableItemRendererParts : RenderingParts(RepairingTableItemRenderer) {
    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val LORE: RenderingPart<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingParts.LORE(this)
}
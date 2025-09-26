package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 用于生成 [`minecraft:tooltip_display`](https://minecraft.wiki/w/Data_component_format#tooltip_display) 物品组件.
 */
@ConfigSerializable
data class MetaTooltipDisplay(
    private val hideTooltip: Boolean = false,
    private val hiddenComponents: List<Key> = emptyList(),
) : ItemMetaEntry<TooltipDisplay> {

    override fun make(context: ItemGenerationContext): ItemMetaResult<TooltipDisplay> {
        val tooltipDisplay = TooltipDisplay.tooltipDisplay()
            .hideTooltip(hideTooltip)
            .hiddenComponents(hiddenComponents.mapNotNull { Registry.DATA_COMPONENT_TYPE.get(it) }.toSet())
            .build()
        return ItemMetaResult.of(tooltipDisplay)
    }

    override fun write(value: TooltipDisplay, itemstack: MojangStack) {
        itemstack.asBukkitMirror().setData(DataComponentTypes.TOOLTIP_DISPLAY, value)
    }
}
package cc.mewcraft.wakame.item.display

import cc.mewcraft.wakame.bridge.MojangStack
import cc.mewcraft.wakame.item.display.implementation.crafting_station.CraftingStationContext
import cc.mewcraft.wakame.item.display.implementation.crafting_station.CraftingStationItemRenderer
import cc.mewcraft.wakame.item.display.implementation.merging_table.MergingTableContext
import cc.mewcraft.wakame.item.display.implementation.merging_table.MergingTableItemRenderer
import cc.mewcraft.wakame.item.display.implementation.modding_table.ModdingTableContext
import cc.mewcraft.wakame.item.display.implementation.modding_table.ModdingTableItemRenderer
import cc.mewcraft.wakame.item.display.implementation.repairing_table.RepairingTableItemRenderer
import cc.mewcraft.wakame.item.display.implementation.repairing_table.RepairingTableItemRendererContext
import cc.mewcraft.wakame.item.display.implementation.rerolling_table.RerollingTableContext
import cc.mewcraft.wakame.item.display.implementation.rerolling_table.RerollingTableItemRenderer
import cc.mewcraft.wakame.item.display.implementation.simple.SimpleItemRenderer
import cc.mewcraft.wakame.item.display.implementation.standard.StandardItemRenderer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 所有的 [ItemRenderers] 实例.
 */
internal object ItemRenderers {
    @JvmField
    val SIMPLE: ItemRenderer<ItemStack, Nothing> = SimpleItemRenderer

    @JvmField // 省去无用的函数调用
    val STANDARD: ItemRenderer<MojangStack, Player> = StandardItemRenderer

    @JvmField
    val CRAFTING_STATION: ItemRenderer<ItemStack, CraftingStationContext> = CraftingStationItemRenderer

    @JvmField
    val MERGING_TABLE: ItemRenderer<ItemStack, MergingTableContext> = MergingTableItemRenderer

    @JvmField
    val MODDING_TABLE: ItemRenderer<ItemStack, ModdingTableContext> = ModdingTableItemRenderer

    @JvmField
    val REROLLING_TABLE: ItemRenderer<ItemStack, RerollingTableContext> = RerollingTableItemRenderer

    @JvmField
    val REPAIRING_TABLE: ItemRenderer<ItemStack, RepairingTableItemRendererContext> = RepairingTableItemRenderer
}

package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.PortableObject
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.reforge.modding.CurseModdingSession
import cc.mewcraft.wakame.reforge.modding.ModdingSession
import cc.mewcraft.wakame.reforge.modding.ModdingTable
import cc.mewcraft.wakame.util.hideAllFlags
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * 定制*诅咒*的主菜单.
 */
class CurseModdingMenu(
    override val table: ModdingTable,
    override val viewer: Player,
) : ModdingMenu<Curse>(), KoinComponent {
    override val logger: Logger by inject()

    override fun createModdingSession(viewer: Player, input: NekoStack): ModdingSession<Curse>? {
        val inputType = input.key
        val cells = input.components.get(ItemComponentTypes.CELLS) ?: run {
            logger.error("No cells found in input item: '$input'. This is a bug!")
            return null
        }

        // 如果没有定制规则, 则判定为无法定制
        val itemRule = table.itemRules[inputType] ?: return null
        val recipeMap = CurseModdingSession.RecipeSessionMap()
        for ((id, cell) in cells) {

            // 如果词条栏没有对应的规则, 则该词条栏不会出现在 GUI 中
            val cellRule = itemRule.cellRules[id] ?: continue

            val name = cell.provideTooltipName().content
            val lore = cell.provideTooltipLore().content
            val recipeDisplay = CurseModdingSession.RecipeSession.Display(name, lore)
            val recipe = CurseModdingSession.RecipeSession(id, cellRule, recipeDisplay)
            recipeMap.put(id, recipe)
        }
        return CurseModdingSession(viewer, input, recipeMap)
    }

    override fun createRecipeMenu(
        parentMenu: ModdingMenu<Curse>, viewer: Player, recipeSession: ModdingSession.RecipeSession<Curse>,
    ): RecipeMenu<Curse> {
        return CurseRecipeMenu(viewer, parentMenu, recipeSession)
    }
}

/**
 * 子菜单, 用于定制单个词条栏里的*诅咒*.
 */
class CurseRecipeMenu(
    viewer: Player,
    parentMenu: ModdingMenu<Curse>,
    recipeSession: ModdingSession.RecipeSession<Curse>,
) : RecipeMenu<Curse>(
    viewer, parentMenu, recipeSession
), KoinComponent {
    override fun viewItemConstructor(recipeSession: ModdingSession.RecipeSession<Curse>): Item {
        return ViewItem(recipeSession)
    }

    override fun getPortableObject(stack: NekoStack): PortableObject<Curse>? {
        return stack.components.get(ItemComponentTypes.PORTABLE_CURSE)
    }

    /**
     * 用于预览*诅咒*的 [Item].
     */
    class ViewItem(
        private val recipeSession: ModdingSession.RecipeSession<Curse>,
    ) : AbstractItem() {

        // 临时实现, 用于方便预览
        private companion object {
            val trims: List<Material> = Tag.ITEMS_TRIM_TEMPLATES.values.toList()
        }

        private fun getTrimMaterial(): Material {
            val sessionHash = recipeSession.hashCode()
            val index = sessionHash % trims.size
            return trims[index]
        }

        override fun getItemProvider(): ItemProvider {
            val stack = ItemStack(getTrimMaterial()).hideAllFlags()
            recipeSession.display.apply(stack)
            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val whoClicked = event.whoClicked
            whoClicked.sendMessage("Clicked slot number: ${event.slot}")
        }
    }
}
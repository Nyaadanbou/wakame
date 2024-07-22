package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.PortableObject
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.reforge.modding.session.ModdingSession
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * 定制*核心*的主菜单.
 */
class CoreModdingMenu(
    override val viewer: Player,
) : ModdingMenu<Core>() {
    override fun recipeMenuConstructor(
        mainMenu: ModdingMenu<Core>,
        viewer: Player,
        recipe: ModdingSession.Recipe<Core>,
    ): RecipeMenu<Core> {
        return CoreRecipeMenu(viewer, mainMenu, recipe)
    }
}

/**
 * 子菜单, 用于定制单个词条栏里的*核心*.
 */
class CoreRecipeMenu(
    override val viewer: Player,
    override val parentMenu: ModdingMenu<Core>,
    override val targetRecipe: ModdingSession.Recipe<Core>,
) : RecipeMenu<Core>() {
    override fun viewItemConstructor(recipe: ModdingSession.Recipe<Core>): Item {
        return ViewItem(targetRecipe)
    }

    override fun getPortableObject(stack: NekoStack): PortableObject<Core>? {
        return stack.components.get(ItemComponentTypes.PORTABLE_CORE)
    }

    /**
     * 用于预览*核心*的 [Item].
     */
    class ViewItem(
        private val recipe: ModdingSession.Recipe<Core>,
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val stack = ItemStack(Material.DIAMOND)
            recipe.display.apply(stack)
            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val whoClicked = event.whoClicked
            whoClicked.sendMessage("Clicked slot number: ${event.slot}")
        }
    }
}
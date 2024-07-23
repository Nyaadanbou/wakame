package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.PortableObject
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.reforge.modding.session.ModdingSession
import cc.mewcraft.wakame.util.ThreadLocalCyclingCounter
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
 * 定制*核心*的主菜单.
 */
class CoreModdingMenu(
    override val viewer: Player,
) : ModdingMenu<Core>(), KoinComponent {
    override val logger: Logger by inject()
    override fun recipeMenuConstructor(
        parentMenu: ModdingMenu<Core>,
        viewer: Player,
        recipe: ModdingSession.Recipe<Core>,
    ): RecipeMenu<Core> {
        return CoreRecipeMenu(viewer, parentMenu, recipe)
    }
}

/**
 * 子菜单, 用于定制单个词条栏里的*核心*.
 */
class CoreRecipeMenu(
    override val viewer: Player,
    override val parentMenu: ModdingMenu<Core>,
    override val sessionRecipe: ModdingSession.Recipe<Core>,
) : RecipeMenu<Core>() {
    override fun viewItemConstructor(recipe: ModdingSession.Recipe<Core>): Item {
        return ViewItem(this.sessionRecipe)
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

        // 临时实现, 用于方便预览
        private companion object {
            val trims: List<Material> = Tag.ITEMS_TRIM_TEMPLATES.values.toList()
            val cyclingCounter: ThreadLocalCyclingCounter = ThreadLocalCyclingCounter(trims.size)
            fun getTrimMaterial(): Material {
                return trims[cyclingCounter.next()]
            }
        }

        override fun getItemProvider(): ItemProvider {
            val stack = ItemStack(getTrimMaterial())
            recipe.display.apply(stack)
            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val whoClicked = event.whoClicked
            whoClicked.sendMessage("Clicked slot number: ${event.slot}")
        }
    }
}
package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.PortableObject
import cc.mewcraft.wakame.item.components.cells.Curse
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
 * 定制*诅咒*的主菜单.
 */
class CurseModdingMenu(
    override val viewer: Player,
) : ModdingMenu<Curse>(), KoinComponent {
    override val logger: Logger by inject()
    override fun recipeMenuConstructor(
        parentMenu: ModdingMenu<Curse>,
        viewer: Player,
        recipe: ModdingSession.Recipe<Curse>,
    ): RecipeMenu<Curse> {
        return CurseRecipeMenu(viewer, parentMenu, recipe)
    }
}

/**
 * 子菜单, 用于定制单个词条栏里的*诅咒*.
 */
class CurseRecipeMenu(
    override val viewer: Player,
    override val parentMenu: ModdingMenu<Curse>,
    override val sessionRecipe: ModdingSession.Recipe<Curse>,
) : RecipeMenu<Curse>() {
    override fun viewItemConstructor(recipe: ModdingSession.Recipe<Curse>): Item {
        return ViewItem(this.sessionRecipe)
    }

    override fun getPortableObject(stack: NekoStack): PortableObject<Curse>? {
        return stack.components.get(ItemComponentTypes.PORTABLE_CURSE)
    }

    /**
     * 用于预览*诅咒*的 [Item].
     */
    class ViewItem(
        private val recipe: ModdingSession.Recipe<Curse>,
    ) : AbstractItem() {

        // 临时实现, 用于方便预览
        private companion object {
            val trims: List<Material> = Tag.ITEMS_TRIM_MATERIALS.values.toList()
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
package cc.mewcraft.wakame.gui.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.PortableObject
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.reforge.modding.config.ModdingTable
import cc.mewcraft.wakame.reforge.modding.session.CoreModdingSession
import cc.mewcraft.wakame.reforge.modding.session.ModdingSession
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
 * 定制*核心*的主菜单.
 */
class CoreModdingMenu(
    override val table: ModdingTable,
    override val viewer: Player,
) : ModdingMenu<Core>(), KoinComponent {
    override val logger: Logger by inject()

    override fun createModdingSession(viewer: Player, input: NekoStack): ModdingSession<Core>? {
        // 开发日记 2024/7/23
        // 当创建一个会话时, 需要先识别这个输入的物品类型 (namespace:path),
        // 然后通过定制台的配置文件, 获取到这个物品的定制规则 (RecipeMap),
        // 然后将这个物品的定制规则传递给会话.

        val inputType = input.key
        val cells = input.components.get(ItemComponentTypes.CELLS) ?: throw IllegalArgumentException("Null cells")
        val itemRule = table.itemRules[inputType] ?: return null
        val recipeMap = CoreModdingSession.RecipeSessionMap()
        for ((id, cell) in cells) {
            val cellRule = itemRule.cellRules[id] ?: continue
            val name = cell.provideTooltipName().content
            val lore = cell.provideTooltipLore().content
            val recipeDisplay = CoreModdingSession.RecipeSession.Display(name, lore)
            val recipe = CoreModdingSession.RecipeSession(id, cellRule, recipeDisplay)
            recipeMap.put(id, recipe)
        }
        return CoreModdingSession(viewer, input, recipeMap)
    }

    override fun createRecipeMenu(
        parentMenu: ModdingMenu<Core>, viewer: Player, recipeSession: ModdingSession.RecipeSession<Core>,
    ): RecipeMenu<Core> {
        return CoreRecipeMenu(viewer, parentMenu, recipeSession)
    }
}

/**
 * 子菜单, 用于定制单个词条栏里的*核心*.
 */
class CoreRecipeMenu(
    viewer: Player,
    parentMenu: ModdingMenu<Core>,
    recipeSession: ModdingSession.RecipeSession<Core>,
) : RecipeMenu<Core>(
    viewer, parentMenu, recipeSession
), KoinComponent {
    override fun viewItemConstructor(recipeSession: ModdingSession.RecipeSession<Core>): Item {
        return ViewItem(recipeSession)
    }

    override fun getPortableObject(stack: NekoStack): PortableObject<Core>? {
        return stack.components.get(ItemComponentTypes.PORTABLE_CORE)
    }

    /**
     * 用于预览*核心*的 [Item].
     */
    class ViewItem(
        private val recipeSession: ModdingSession.RecipeSession<Core>,
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
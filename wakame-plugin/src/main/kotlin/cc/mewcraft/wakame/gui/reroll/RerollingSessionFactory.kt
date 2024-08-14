package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.reforge.reroll.SimpleRerollingSession

internal object RerollingSessionFactory {
    private const val PREFIX = ReforgeLoggerPrefix.REROLL

    /**
     * 构建一个 [RerollingSession].
     *
     * 返回 `null` 表示 [input] 不支持重造.
     */
    fun create(
        menu: RerollingMenu,
        input: NekoStack,
    ): RerollingSession? {
        val templates = input.templates.get(ItemTemplateTypes.CELLS)?.cells ?: run {
            // 如果这个物品没有对应的词条栏模板, 则判定整个物品不支持重造
            menu.logger.warn("$PREFIX Input item has no `cells` templates. This might be a config issue!")
            return null
        }

        // 如果这个物品没有词条栏组件, 则判定整个物品不支持重造
        val cells = input.components.get(ItemComponentTypes.CELLS) ?: return null

        // 如果这个物品没有对应的重造规则, 则判定整个物品不支持重造
        val itemRule = menu.table.itemRules[input.key] ?: return null

        val selections = SimpleRerollingSession.SelectionMap()
        for ((id, cell) in cells) {

            // 如果这个词条栏没有对应的重造规则, 则判定该词条栏不支持重造
            val cellRule = itemRule.cellRules[id] ?: continue

            // 这个词条栏没有对应的模板, 则判定该词条栏不支持重造
            val template = templates[id]?.core ?: continue

            val display = SimpleRerollingSession.SelectionDisplay(
                name = cell.provideTooltipName().content,
                lore = cell.provideTooltipLore().content,
            )

            val selection = SimpleRerollingSession.Selection(
                id = id,
                rule = cellRule,
                template = template,
                display = display,
            )
            selections[id] = selection
        }

        return SimpleRerollingSession(
            table = menu.table,
            viewer = menu.viewer,
            inputItem = input,
            selections = selections
        )
    }
}
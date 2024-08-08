package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.reforge.modding.ModdingSession
import cc.mewcraft.wakame.reforge.modding.SimpleModdingSession

internal object ModdingSessionFactory {
    /**
     * 创建一个 [ModdingSession].
     *
     * 返回 `null` 表示 [input] 不支持定制.
     */
    fun create(
        menu: ModdingMenu,
        input: NekoStack,
    ): ModdingSession? {
        // 开发日记 2024/7/23
        // 当创建一个会话时, 需要先识别这个输入的物品类型 (namespace:path),
        // 然后通过定制台的配置文件, 获取到这个物品的定制规则 (ReplaceMap),
        // 然后将这个物品的定制规则传递给会话.

        val cells = input.components.get(ItemComponentTypes.CELLS) ?: run {
            // 如果没有词条栏, 则判定为无法定制
            menu.logger.error("No cells found in input item: '$input'. This is a bug!")
            return null
        }

        // 如果没有定制规则, 则判定为无法定制
        val itemRule = menu.table.itemRules[input.key] ?: return null

        val replaceMap = SimpleModdingSession.ReplaceMap()
        for ((id, cell) in cells) {

            // 如果词条栏不存在对应的规则, 则该词条栏不会出现在 GUI 中
            val cellRule = itemRule.cellRules[id] ?: continue

            val display = SimpleModdingSession.Replace.Display(
                cell.provideTooltipName().content,
                cell.provideTooltipLore().content
            )

            val replace = SimpleModdingSession.Replace(id, cellRule, display)

            replaceMap[id] = replace
        }

        return SimpleModdingSession(menu.viewer, input, replaceMap)
    }
}
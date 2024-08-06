package cc.mewcraft.wakame.reforge.rerolling

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Group
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

/**
 * 代表一个物品重造的过程, 封装了一次重造所需要的所有状态.
 */
interface RerollingSession : Examinable {
    val viewer: Player
    val inputNekoStack: NekoStack
    var outputNekoStack: NekoStack?
    val selectionSessions: SelectionSessionMap
    var confirmed: Boolean
    var frozen: Boolean
    fun reforge(): Result
    fun clearSelections()
    fun returnInput(viewer: Player)

    interface Result : Examinable {
        // 花费
        val cost: TotalCost

        // 克隆
        val item: NekoStack

        interface TotalCost {
            // 默认货币的花费
            val default: Double

            // 其他货币的花费
            fun get(currency: String): Double
        }
    }

    interface SelectionSession : Examinable {
        val id: String
        val rule: RerollingTable.CellRule

        // 用于重新随机的 Group, 将从 NekoItem 获取
        val group: Group<TemplateCore, GenerationContext>

        val display: Display

        // true 表示玩家选择重造该词条栏
        var selected: Boolean

        interface Display : Examinable {
            val name: Component
            val lore: List<Component>

            @Contract(pure = false)
            fun apply(item: ItemStack)
        }
    }

    interface SelectionSessionMap : Examinable, Iterable<Map.Entry<String, SelectionSession>> {
        val size: Int
        fun get(id: String): SelectionSession?
        fun put(id: String, session: SelectionSession)
        fun contains(id: String): Boolean
    }
}
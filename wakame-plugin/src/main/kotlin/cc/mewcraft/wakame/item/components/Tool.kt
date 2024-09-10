package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import net.kyori.adventure.util.TriState
import net.kyori.examination.Examinable
import org.bukkit.Material

data class Tool(
    val defaultMiningSpeed: Float,
    val damagePerBlock: Int,
    val rules: List<Rule>,
) : Examinable {

    data class Rule(
        val blockTypes: Collection<Material>,
        val speed: Float?,
        val correctForDrops: TriState,
    )

    companion object : ItemComponentBridge<Tool> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.TOOL)

        override fun codec(id: String): ItemComponentType<Tool> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Tool> {
        override fun read(holder: ItemComponentHolder): Tool? {
            val craftTool = holder.item.itemMeta?.tool ?: return null
            val defaultMiningSpeed = craftTool.defaultMiningSpeed
            val damagePerBlock = craftTool.damagePerBlock
            val rules = craftTool.rules.mapNotNull { craftRule -> Rule(craftRule.blocks, craftRule.speed, TriState.byBoolean(craftRule.isCorrectForDrops)) }
            return Tool(
                defaultMiningSpeed = defaultMiningSpeed,
                damagePerBlock = damagePerBlock,
                rules = rules
            )
        }

        override fun write(holder: ItemComponentHolder, value: Tool) {
            val craftTool = holder.item.itemMeta?.tool ?: return
            craftTool.defaultMiningSpeed = value.defaultMiningSpeed
            craftTool.damagePerBlock = value.damagePerBlock
            // craftTool.rules = ... // FIXME 支持向物品写入 Rules
            holder.item.itemMeta?.setTool(craftTool)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.itemMeta?.setTool(null)
        }
    }
}

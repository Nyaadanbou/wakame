package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.util.TriState
import net.kyori.examination.Examinable
import org.bukkit.Material
import io.papermc.paper.datacomponent.item.Tool as PaperTool

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
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: Tool) {
            val defaultMiningSpeed = value.defaultMiningSpeed
            val damagePerBlock = value.damagePerBlock
            val rules = value.rules
            val paperRules = rules.map { rule ->
                val blocks = RegistrySet.keySetFromValues(RegistryKey.BLOCK, rule.blockTypes.mapNotNull(Material::asBlockType))
                val speed = rule.speed
                val correctForDrops = rule.correctForDrops
                PaperTool.rule(blocks, speed, correctForDrops)
            }
            val paperTool = PaperTool.tool()
                .defaultMiningSpeed(defaultMiningSpeed)
                .damagePerBlock(damagePerBlock)
                .addRules(paperRules)
            holder.bukkitStack.setData(DataComponentTypes.TOOL, paperTool)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}

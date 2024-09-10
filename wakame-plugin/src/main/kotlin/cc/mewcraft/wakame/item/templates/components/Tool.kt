package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.Tool.Rule
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.util.TriState
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import cc.mewcraft.wakame.item.components.Tool as ToolData


data class Tool(
    val defaultMiningSpeed: Float,
    val damagePerBlock: Int,
    val rules: List<Rule>,
) : ItemTemplate<ToolData> {
    override val componentType: ItemComponentType<ToolData> = ItemComponentTypes.TOOL

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ToolData> {
        val raw = ToolData(defaultMiningSpeed, damagePerBlock, rules)
        return ItemGenerationResult.of(raw)
    }

    companion object : ItemTemplateBridge<Tool> {
        override fun codec(id: String): ItemTemplateType<Tool> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<Tool> {
        override val type: TypeToken<Tool> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   default_mining_speed: 4.0
         *   damage_per_block: 1
         *   rules:
         *     - block_types: ['minecraft:stone']
         *       speed: 1.0
         *       correct_for_drops: true
         *     - block_types: ['minecraft:dirt', 'minecraft:grass']
         *       speed: 2.0
         *       correct_for_drops: true
         * ```
         */
        override fun decode(node: ConfigurationNode): Tool {
            // optional
            val defaultMiningSpeed = node.node("default_mining_speed").getFloat(1F)
            // optional
            val damagePerBlock = node.node("damage_per_block").getInt(1)
            // optional
            val rules = node.node("rules").childrenList().map { child ->
                val blockTypes = child.node("block_types")
                    .getList<String>(emptyList())
                    .mapNotNull(NamespacedKey::fromString)
                    .mapNotNull { Material.matchMaterial(it.asString()) }
                val speed = child.node("speed").get<Float>()
                val correctForDrops = child.node("correct_for_drops").get<TriState>(TriState.NOT_SET)
                Rule(blockTypes, speed, correctForDrops)
            }

            return Tool(defaultMiningSpeed, damagePerBlock, rules)
        }
    }
}
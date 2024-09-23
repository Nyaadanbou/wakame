package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.util.TriState
import net.kyori.examination.Examinable
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList

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

    companion object : ItemComponentBridge<Tool>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<Tool> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.TOOL
        override val tooltipKey: TooltipKey = ItemConstants.createKey { TOOL }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
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

        private companion object
    }

    data class Template(
        val defaultMiningSpeed: Float,
        val damagePerBlock: Int,
        val rules: List<Rule>,
    ) : ItemTemplate<Tool> {
        override val componentType: ItemComponentType<Tool> = ItemComponentTypes.TOOL

        override fun generate(context: GenerationContext): GenerationResult<Tool> {
            val raw = Tool(defaultMiningSpeed, damagePerBlock, rules)
            return GenerationResult.of(raw)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

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
        override fun decode(node: ConfigurationNode): Template {
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

            return Template(defaultMiningSpeed, damagePerBlock, rules)
        }
    }
}

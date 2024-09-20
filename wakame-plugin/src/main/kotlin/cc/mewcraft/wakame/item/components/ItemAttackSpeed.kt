package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode

data class ItemAttackSpeed(
    /**
     * 攻速等级.
     */
    val level: AttackSpeedLevel
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<ItemAttackSpeed>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<ItemAttackSpeed> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.ATTACK_SPEED
        override val tooltipKey: TooltipKey = ItemComponentConstants.createKey { ATTACK_SPEED }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.DiscreteTooltips = config.DiscreteTooltips()
    }

    override fun provideTooltipLore(): LoreLine {
        if (!config.showInTooltip) {
            return LoreLine.noop()
        }
        return LoreLine.simple(tooltipKey, listOf(tooltip.render(level.ordinal)))
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemAttackSpeed> {
        override fun read(holder: ItemComponentHolder): ItemAttackSpeed? {
            val tag = holder.getTag() ?: return null
            val level = tag.getByte(TAG_KEY)
            return ItemAttackSpeed(AttackSpeedLevel.entries[level.toInt()])
        }

        override fun write(holder: ItemComponentHolder, value: ItemAttackSpeed) {
            holder.editTag { tag ->
                tag.putByte(TAG_KEY, value.level.ordinal.toByte())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        companion object {
            const val TAG_KEY = "level"
        }
    }

    data class Template(
        /**
         * 攻速等级.
         */
        val level: AttackSpeedLevel
    ) : ItemTemplate<ItemAttackSpeed> {
        override val componentType: ItemComponentType<ItemAttackSpeed> = ItemComponentTypes.ATTACK_SPEED

        override fun generate(context: GenerationContext): GenerationResult<ItemAttackSpeed> {
            return GenerationResult.of(ItemAttackSpeed(level))
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <level>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            val raw = node.krequire<AttackSpeedLevel>()
            return Template(raw)
        }
    }
}
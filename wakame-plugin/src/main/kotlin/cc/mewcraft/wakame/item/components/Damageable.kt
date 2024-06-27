package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import org.bukkit.inventory.meta.Damageable as BukkitDamageable

interface Damageable : Examinable, TooltipProvider {

    /**
     * 当前损耗.
     */
    val damage: Int

    /**
     * 最大损耗.
     */
    val maxDamage: Int

    data class Value(
        override val damage: Int,
        override val maxDamage: Int,
    ) : Damageable {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.DAMAGEABLE) {
            val tooltipKey: TooltipKey = ItemComponentConstants.createKey { DAMAGEABLE }
            val tooltipText: SingleTooltip = SingleTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Damageable, ItemComponentHolder.Complex> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Complex): Damageable? {
            val itemMeta = (holder.item.itemMeta as? BukkitDamageable) ?: return null
            return Value(
                damage = itemMeta.damage,
                maxDamage = itemMeta.maxDamage,
            )
        }

        override fun write(holder: ItemComponentHolder.Complex, value: Damageable) {
            holder.item.editMeta<BukkitDamageable> {
                this.damage = value.damage
                this.setMaxDamage(value.maxDamage)
            }
        }

        override fun remove(holder: ItemComponentHolder.Complex) {
            holder.item.editMeta<BukkitDamageable> {
                this.setMaxDamage(null)
            }
        }

        private companion object
    }

    data class Template(
        val damage: RandomizedValue,
        val maxDamage: RandomizedValue,
        val disappearWhenBroken: Boolean,
    ) : ItemTemplate<Value> {
        override fun generate(context: GenerationContext): GenerationResult<Value> {
            TODO("Not yet implemented")
        }

        companion object : ItemTemplateType<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}
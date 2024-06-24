package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentTemplate
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.editMeta
import cc.mewcraft.wakame.util.getListOrNull
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.StringShadowTag
import net.kyori.adventure.key.Key
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

    /**
     * 损耗达到最大损耗时, 物品是否消失.
     */
    val unbreakable: Boolean

    /**
     * 修复该物品所需要的材料.
     */
    val repairItems: List<Key>

    data class Value(
        override val damage: Int,
        override val maxDamage: Int,
        override val unbreakable: Boolean,
        override val repairItems: List<Key>,
    ) : Damageable {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }

        companion object : ItemComponentConfig(ItemComponentConstants.DAMAGEABLE) {
            val tooltipKey: TooltipKey = ItemComponentConstants.createKey { DAMAGEABLE }
            val tooltipText: SingleTooltip = SingleTooltip()
        }
    }

    class Codec(
        override val id: String,
    ) : ItemComponentType<Damageable, ItemComponentHolder.Complex> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Complex): Damageable? {
            // TODO DataComponent API 推出后重写
            val itemMeta = (holder.item.itemMeta as? BukkitDamageable) ?: return null
            val repairItems = holder.tag.getListOrNull("repair_items", ShadowTagType.STRING)?.map { Key((it as StringShadowTag).value()) } ?: return null
            return Value(
                damage = itemMeta.damage,
                maxDamage = itemMeta.maxDamage,
                unbreakable = itemMeta.isUnbreakable,
                repairItems = repairItems
            )
        }

        override fun write(holder: ItemComponentHolder.Complex, value: Damageable) {
            // TODO DataComponent API 推出后重写
            holder.item.editMeta<BukkitDamageable> {
                this.damage = value.damage
                this.setMaxDamage(value.maxDamage)
            }
        }

        override fun remove(holder: ItemComponentHolder.Complex) {
            // TODO DataComponent API 推出后重写
            holder.item.editMeta<BukkitDamageable> {
                this.setMaxDamage(null)
            }
        }
    }

    data class Template(
        val damage: RandomizedValue,
        val maxDamage: RandomizedValue,
        val unbreakable: Boolean,
        val repairItems: List<Key>,
    ) : ItemComponentTemplate<Value> {
        override fun generate(context: GenerationContext): GenerationResult<Value> {
            TODO("Not yet implemented")
        }

        companion object : ItemComponentTemplate.Serializer<Arrow.Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Arrow.Template {
                TODO("Not yet implemented")
            }
        }
    }
}
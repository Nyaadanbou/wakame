package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.ItemAttackSpeed as ItemAttackSpeedData

data class ItemAttackSpeed(
    /**
     * 攻速等级.
     */
    val level: AttackSpeedLevel,
) : ItemTemplate<ItemAttackSpeedData> {
    override val componentType: ItemComponentType<ItemAttackSpeedData> = ItemComponentTypes.ATTACK_SPEED

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemAttackSpeedData> {
        return ItemGenerationResult.of(ItemAttackSpeedData(level))
    }

    companion object : ItemTemplateBridge<ItemAttackSpeed> {
        override fun codec(id: String): ItemTemplateType<ItemAttackSpeed> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemAttackSpeed> {
        override val type: TypeToken<ItemAttackSpeed> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <level>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemAttackSpeed {
            val raw = node.require<AttackSpeedLevel>()
            return ItemAttackSpeed(raw)
        }
    }
}